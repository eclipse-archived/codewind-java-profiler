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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ibm.JavaProfilingLanguageServer.ProfilingUtils;

public class ProfilingUtilsTest {
    String dockerPath = "/profiling/workspace/";
    String fileRelativePath = "project/file.java";

    @Test
    @DisplayName("DockerizeFilePathUsingMountedVolume should convert local file path to Docker file path")
    public void DockerizeFilePathUsingMountedVolumeShouldConvertLocalFilePathToDockerFilePathTest() {
        String localPath = "/Users/test/workspace/";

        String localFilePath = localPath + fileRelativePath;
        String binding = localPath + ":" + dockerPath;

        String finalPath = ProfilingUtils.dockerizeFilePathUsingMountedVolume(localFilePath, binding);
        // assert statements
        assertEquals(dockerPath + fileRelativePath, finalPath);
    }

    @Test
    @DisplayName("DockerizeFilePathUsingMountedVolume should handle HTML escape codes")
    public void DockerizeFilePathUsingMountedVolumeShouldHandleHTMLEscapeCodes() throws UnsupportedEncodingException {
        String localPath = "/Users/test@ibm.com/workspace/";
        String localHTMLEscapedPath = URLEncoder.encode(localPath, "UTF-8");

        String localFilePath = localHTMLEscapedPath + fileRelativePath;
        String binding = localPath + ":" + dockerPath;

        String finalPath = ProfilingUtils.dockerizeFilePathUsingMountedVolume(localFilePath, binding);
        // assert statements
        assertEquals(dockerPath + fileRelativePath, finalPath);
    }
}
