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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.eclipse.lsp4j.WorkspaceFolder;

public class ProfilingUtils {

	public final static String LOAD_TEST_DIRECTORY = "load-test";
	public static String[] mountedVolumes = {};

	/**
	 * Converts the external path to a path recognised within the docker container
	 *
	 * @param path - the path to update
	 * @return - a new path string which should be able to be resolved within the
	 *         docker container
	 */
	public static String dockerizeFilePath(String path) {
		path = removeFileProtocol(path);

		for (String mountedVolume : mountedVolumes) {
			path = dockerizeFilePathUsingMountedVolume(path, mountedVolume);
		}

		return path;
	}

	public static String dockerizeFilePathUsingMountedVolume(String path, String mount) {
		// try to re-add special characters to the path string
		String unescapedPath;
		try {
			unescapedPath = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			unescapedPath = path;
		}

		// the workspace passed in should be in format /host/path:/docker/path
		String[] mounts = mount.replace("\"", "").split(":");
		String hostWorkspace = mounts[0];
		String dockerWorkspace = mounts[1];
		String newString = unescapedPath.replace(hostWorkspace, dockerWorkspace);

		System.out.println("dockerizeFilePathUsingMountedVolume: " + newString + "\n");

		return newString;
	}

	public static List<WorkspaceFolder> dockerizeWorkspaceFolderPaths(List<WorkspaceFolder> workspaceFolders) {
		// update folder paths for Docker container
		if(workspaceFolders != null) {
			for (WorkspaceFolder folder : workspaceFolders) {
				folder.setUri(ProfilingUtils.dockerizeFilePath(folder.getUri()));
			}
		}

		return workspaceFolders;
	}

	/**
	 * Removes the additional bits added to the start of the file paths, e.g. `file://` to give a file path which can be used
	 * @param path File path to be changed
	 * @return file path without any additional text at the start
	 */
	public static String removeFileProtocol(String path) {
		return path.replace("file://", "").replace("file:", "");
	}
}
