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
