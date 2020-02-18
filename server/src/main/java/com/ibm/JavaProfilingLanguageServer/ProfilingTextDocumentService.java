/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.JavaProfilingLanguageServer;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

public class ProfilingTextDocumentService implements TextDocumentService {

	private LanguageClient client;
	private List<String> projectDirs;
	private List<WorkspaceFolder> workspaceFolders;

	public ProfilingTextDocumentService(LanguageClient client, List<WorkspaceFolder> workspaceFolders) {
		this.client = client;
		this.workspaceFolders = workspaceFolders;

		// get project directories
		projectDirs = getCodewindProjectFolders();
	}

	private List<String> getCodewindProjectFolders() {
		List<String> mcProjectFolders = new ArrayList<String>();

		for (WorkspaceFolder folder : workspaceFolders) {
			File currentFile = new File(folder.getUri());
			System.out.println("Workspace Folder: " + folder.getUri() + "\n");
			mcProjectFolders.addAll(searchForFolders(currentFile, ProfilingUtils.LOAD_TEST_DIRECTORY));
		}

		// remove load test directory from project path
		for (int i = 0; i < mcProjectFolders.size(); i++) {
			mcProjectFolders.set(i, mcProjectFolders.get(i).replace(ProfilingUtils.LOAD_TEST_DIRECTORY, ""));
		}

		System.out.println("Project Folders:");
		System.out.println(mcProjectFolders + "\n");
		return mcProjectFolders;
	}

	private List<String> searchForFolders(File currentDir, String targetDirName) {
		List<String> projectPaths = new ArrayList<String>();

		File currentPath = new File(currentDir.getPath());
		if(currentPath.isDirectory()) {
			if(currentPath.getName().equals(targetDirName)) {
				projectPaths.add(currentPath.getPath());
			} else if(!currentPath.getName().startsWith(".") && !currentPath.getPath().contains("node_modules")) {
				File[] childFiles = currentPath.listFiles();
				for (File file : childFiles) {
					projectPaths.addAll(searchForFolders(file, targetDirName));
				}
			}
		}
		return projectPaths;
	}


	public void didOpen(DidOpenTextDocumentParams params) {
		TextDocumentItem document = params.getTextDocument();
		System.out.printf("didOpen: %s%n%n", document.getUri());

		publishProfilingDiagnostics(document);
	}

	private void publishProfilingDiagnostics(TextDocumentItem textDocument) {

		// create diagnostics object and link it to current file
		PublishDiagnosticsParams diagnosticParams = new PublishDiagnosticsParams();
		diagnosticParams.setUri(textDocument.getUri());

		// convert URI to docker version
		textDocument.setUri(ProfilingUtils.dockerizeFilePath(textDocument.getUri()));

		String projectPath = getProjectForPath(textDocument.getUri());
		System.out.println("Project Path for file: " + projectPath + "\n");


		// If our settings have changed we need to send back empty diagnostics to clear
	  // any previous diagnostics.
		if(projectPath == null) {
			resetDiagnosticsForFile(textDocument);
			return;
		}

		String loadTestResults = getLatestLoadTestResults(projectPath);

		List<Diagnostic> diagnostics = getDiagnosticsForFile(textDocument, loadTestResults);
		diagnosticParams.setDiagnostics(diagnostics);

		System.out.println("Sending diagnostics");
		// send diagnostics to the client
		client.publishDiagnostics(diagnosticParams);
	}

	private String getProjectForPath(String path) {
		for (String projectPath : projectDirs) {
			if(path.startsWith(projectPath)) {
				return projectPath;
			}
		}

		return null;
	}

	private String getLatestLoadTestResults(String projectDir) {
		File loadTestDir = new File(Paths.get(projectDir, ProfilingUtils.LOAD_TEST_DIRECTORY).toUri());
		System.out.println("searching for latest load test results");
		List<File> loadResultDirs = new ArrayList<File>();

		for (File file : loadTestDir.listFiles()) {
			if(file.isDirectory() && !file.getName().startsWith(".")) {
				loadResultDirs.add(file);
			}
		}

		Collections.sort(loadResultDirs);
		System.out.println("Result: " + loadResultDirs + "\n");

		int noOfDirsFound = loadResultDirs.size();

		return noOfDirsFound > 0 ? loadResultDirs.get(noOfDirsFound - 1).getPath() : null;
	}

	private List<Diagnostic> getDiagnosticsForFile(TextDocumentItem textDocument, String loadTestResults) {
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();

		String documentPackage = getDocumentPackage(textDocument);
//		System.out.println("documentPackage: " + documentPackage);

		File hcd = getHCDFromLoadTestDir(loadTestResults);

		// if not found
		if(hcd == null) {
			return new ArrayList<Diagnostic>();
		}

		HCDProfiler hcdProfiler = new HCDProfiler(hcd);
		List<HotMethod> hotMethods = hcdProfiler.analyseHCD(documentPackage);

		for (HotMethod hotMethod : hotMethods) {
			Range diagnosticRange = hotMethod.findInTextDocument(textDocument);

			String message = String.format("Method %s() was the running function during load testing %.1f%% of the time.", hotMethod.getMethodName(), hotMethod.getPercentage());

			Diagnostic d = new Diagnostic();
			d.setSource("Codewind Java Profiler");
			d.setSeverity(DiagnosticSeverity.Information);
			d.setMessage(message);
			d.setRange(diagnosticRange);

			diagnostics.add(d);
		}

		MessageParams mp = new MessageParams();
		mp.setType(MessageType.Log);
		String message = "";
		for (HotMethod method : hotMethods) {
			message += "	" + method;
		}

		mp.setMessage("Codewind Java Profiler - Hot Methods:\n" + message);
		client.logMessage(mp);

		return diagnostics;
	}

	private String getDocumentPackage(TextDocumentItem textDocument) {
		String findPackageString = "^\\s*package\\s+(\\S+);";
		Pattern findPackage = Pattern.compile(findPackageString);
		Matcher m = findPackage.matcher(textDocument.getText());
		return m.find() ? m.group(1) : "";
	}

	private File getHCDFromLoadTestDir(String loadTestPath) {
		System.out.println("Searching for HCD file in dir: " + loadTestPath);
		File loadTestDir = new File(loadTestPath);
		if(!loadTestDir.isDirectory()) return null;

		List<File> hcdFiles = new ArrayList<File>();

		for (File file : loadTestDir.listFiles()) {
			if (file.getName().endsWith(".hcd")) {
				hcdFiles.add(file);
			}
		}

		System.out.println("Result: " + hcdFiles + "\n");
		return hcdFiles.size() > 0 ? hcdFiles.get(0) : null;
	}

	private void resetDiagnosticsForFile(TextDocumentItem textDocument) {
		PublishDiagnosticsParams diagnosticParams = new PublishDiagnosticsParams();
		diagnosticParams.setUri(textDocument.getUri());
		diagnosticParams.setDiagnostics(new ArrayList<Diagnostic>());
		client.publishDiagnostics(diagnosticParams);
	}


	public void didChange(DidChangeTextDocumentParams params) {
		System.out.println("didChange:");
		params.getContentChanges().forEach(action -> System.out.println(action));
		System.out.println();
	}


	public void didClose(DidCloseTextDocumentParams params) {
		// TODO Auto-generated method stub
		System.out.println("didClose\n");

	}


	public void didSave(DidSaveTextDocumentParams params) {
		// TODO Auto-generated method stub
		System.out.println("didSave \n");

	}

}
