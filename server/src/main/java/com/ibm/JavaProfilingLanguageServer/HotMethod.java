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

import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;

public class HotMethod {
	private String methodName;
	private String withinFileName; // includes class and any nested functions in format Class.function.function
	private String packageName;
	private String fullName;
	private Double percentage;

	public HotMethod(String fullName, String packageName, Double percentage) {
		this.fullName = fullName.replaceAll("\\([A-Za-z0-9,\\.\\s]*\\)", "");
		this.percentage = percentage;
		this.packageName = packageName.replaceAll("\\([A-Za-z0-9,\\.\\s]*\\)", "");
		this.withinFileName = this.fullName.replace(packageName + ".", "");

		// split on '.' characters outside of brackets
		String[] packageNameArray = withinFileName.split("\\.");

		this.methodName = packageNameArray[packageNameArray.length - 1];
	}

	public String toString() {
		return String.format("methodName: %s, withinFileName: %s, packageName: %s, fullName: %s, percentage: %s",
				this.methodName, this.withinFileName, this.packageName, this.fullName, this.percentage);
	}

	public String getMethodName() {
		return methodName;
	}

	public String getWithinFileName() {
		return withinFileName;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getFullName() {
		return fullName;
	}

	public Double getPercentage() {
		return percentage;
	}

	public Range findInTextDocument(TextDocumentItem textDocument) {
		String[] nameInFile = withinFileName.split("\\.");
		String textWithoutComments = removeCommentsFromText(textDocument.getText());

		Range contextRange = new Range();
		contextRange.setStart(new Position(0,0));
		contextRange.setEnd(new Position(999999, 999999));

		try {
			for (int i = 0; i < nameInFile.length; i++) {
				String context = nameInFile[i];
				contextRange = getRangeInTextWithinScope(context, textWithoutComments, contextRange, i);
			}

			String[] lines = textDocument.getText().split("\\r?\\n");

			int startOfMethodName = contextRange.getStart().getCharacter();

			String findHotMethodString = methodName + "\\(.*\\)";
			Pattern findPackage = Pattern.compile(findHotMethodString);
			Matcher m = findPackage.matcher(lines[contextRange.getStart().getLine()]);
			int endOfMethodName = m.find() ? m.group(0).length() : methodName.length();

			contextRange.setEnd(new Position(contextRange.getStart().getLine(), startOfMethodName + endOfMethodName));

			//		System.out.println("*** FINAL SCOPE ***");
			//		System.out.printf("Range: start: %s:%s, end: %s:%s%n",
			//				contextRange.getStart().getLine(), contextRange.getStart().getCharacter(),
			//				contextRange.getEnd().getLine(), contextRange.getEnd().getCharacter());

		} catch (Exception e) {
			System.out.println("Context not found within file");
		}
		return contextRange;
	}

	private String removeCommentsFromText(String text) {
		text = text.replaceAll("//.*","");
		String[] lines = text.split("\\r?\\n");
		String finalString = "";

		boolean blockComment = false;
		for (int i = 0; i < lines.length; i++) {
			final int blockCommentEndLength = "*/".length();
			String line = lines[i];

			do {
				if(blockComment) {
					// end of multiline comment
					if(line.matches(".*\\*/.*")) {
						int endOfComment = line.indexOf("*/") + blockCommentEndLength;
						line = line.substring(endOfComment);
						blockComment = false;
					} else {
						// still in a block comment make blank
						line = "";
					}
				}

				// start of multiline comment
				if(line.contains("/*")) {
					int startOfComment = line.indexOf("/*");

					// without end i.e. start and end not on same line
					if(!line.substring(startOfComment).contains("*/")) {
						blockComment = true;
						line = line.substring(0, startOfComment);
					} else {
						// if multiline comment starts and ends in same line
						int endOfComment = line.indexOf("*/") + blockCommentEndLength;
						line = line.substring(startOfComment, endOfComment);
					}
				}
			} while(line.contains("/*") || line.contains("*/"));

			finalString += line + "\n";
		}

		return finalString;
	}

	private Range getRangeInTextWithinScope(String scope, String text, Range range, int scopeDepth) {
		// System.out.printf("scope: %s, text: %s, range: %s, scopDepth: %s", scope, text, range, scopeDepth);
		Range scopedRange = new Range();
		// System.out.println("Looking for scope: " + scope);
		String[] lines = text.split("\\r?\\n");
		List<String> scopedLines = new ArrayList<String>();

		boolean scopeFound = false;
		boolean firstBracketFound = false;
		int stepInto = 0;


		for (int i = range.getStart().getLine(); i < Math.min(range.getEnd().getLine(), lines.length); i++) {
			String line = lines[i];

			if(!scopeFound && stepInto == scopeDepth) {
				if(line.contains(scope)) {
					Position start = new Position();
					scopeFound = true;
					start.setLine(i);
					start.setCharacter(line.indexOf(scope));
					scopedRange.setStart(start);
				}
			}

			if(line.contains("{")) {
				if(scopeFound) {
					firstBracketFound = true;
				}
				stepInto ++;
			}

			if(line.contains("}")) {
				stepInto --;
			}

			if(firstBracketFound) {
				scopedLines.add(line);
				if(stepInto == 0) {
					Position end = new Position();
					end.setLine(i);
					end.setCharacter(line.length());
					scopedRange.setEnd(end);
					break;
				}
			}

			// System.out.printf("[%s][%s]: %s%n", stepInto, scopeDepth, line);
		}
		return scopedRange;
	}
}
