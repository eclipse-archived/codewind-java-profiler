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

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;

public class LanguageServerUtils {
	private static LanguageClient client;

	public LanguageServerUtils() {

	}

	public LanguageServerUtils(LanguageClient languageClient) {
		client = languageClient;
	}

	public void logMessage(String message) {
		MessageParams mp = new MessageParams();
		mp.setMessage(message);
		mp.setType(MessageType.Info);
		client.logMessage(mp);
	}
}
