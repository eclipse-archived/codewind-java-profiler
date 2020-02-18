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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;

public class StartServer {

	final static String HOST = System.getenv("CLIENT_HOST");
	final static int PORT = Integer.parseInt(System.getenv("CLIENT_PORT"));
	final static String BINDS = System.getenv("BINDS");

	@SuppressWarnings("resource")
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		InputStream in;
		OutputStream out;

		try {
			ProfilingUtils.mountedVolumes = BINDS.split(",");
			System.out.println("BINDS:");
			System.out.println(ProfilingUtils.mountedVolumes);
			System.out.println();
			Socket socket = new Socket(HOST, PORT);
			in = socket.getInputStream();
			out = socket.getOutputStream();

			LanguageServer server = new ProfilingLanguageServer();
			Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, in, out);
			LanguageClient client = launcher.getRemoteProxy();
			((LanguageClientAware) server).connect(client);
			launcher.startListening();
		} catch(ConnectException e) {
			System.out.printf("Could not connect to client on %s:%s%n", HOST, PORT);
		}
	}
}
