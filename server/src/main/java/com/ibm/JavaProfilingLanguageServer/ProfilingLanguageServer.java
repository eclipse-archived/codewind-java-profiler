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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

public class ProfilingLanguageServer implements TextDocumentService, LanguageServer, LanguageClientAware, WorkspaceService {
	private LanguageClient client;
	private ProfilingTextDocumentService textDocumentService;
	private List<WorkspaceFolder> workspaceFolders;
	private LanguageServerUtils languageServerUtils;

	public void connect(LanguageClient client) {
		this.client = client;
		languageServerUtils = new LanguageServerUtils(this.client);
	}

	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		// get workspace folders
		workspaceFolders = ProfilingUtils.dockerizeWorkspaceFolderPaths(params.getWorkspaceFolders());

		// set server capabilities
		ServerCapabilities capabilities = new ServerCapabilities();
		capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);

		textDocumentService = new ProfilingTextDocumentService(client, workspaceFolders);

		// send the result of the initialization back to the client
		InitializeResult result = new InitializeResult();
		result.setCapabilities(capabilities);

		return CompletableFuture.completedFuture(result);
	}

	public CompletableFuture<Object> shutdown() {
		languageServerUtils.logMessage("SHUTDOWN CALLED\n");
		return null;
	}

	public void exit() {
		languageServerUtils.logMessage("EXIT CALLED\n");
		System.exit(0);
	}

	public TextDocumentService getTextDocumentService() {
		return (TextDocumentService) this;
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		textDocumentService.didOpen(params);
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		textDocumentService.didChange(params);
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		textDocumentService.didClose(params);
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		textDocumentService.didSave(params);
	}

	public WorkspaceService getWorkspaceService() {
		return (WorkspaceService) this;
	}

	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		languageServerUtils.logMessage("didChangeConfiguration\n");
//		languageServerUtils.logMessage(params);
	}

	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		languageServerUtils.logMessage("didChangeWatchedFiles\n");
	}
}
