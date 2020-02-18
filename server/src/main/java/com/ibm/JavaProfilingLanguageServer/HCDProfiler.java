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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.java.diagnostics.healthcenter.api.ConnectionProperties;
import com.ibm.java.diagnostics.healthcenter.api.HealthCenter;
import com.ibm.java.diagnostics.healthcenter.api.HealthCenterException;
import com.ibm.java.diagnostics.healthcenter.api.HealthCenterJMXException;
import com.ibm.java.diagnostics.healthcenter.api.HealthCenterSSLException;
import com.ibm.java.diagnostics.healthcenter.api.classes.ClassesEvent;
import com.ibm.java.diagnostics.healthcenter.api.classes.ClassesEventListener;
import com.ibm.java.diagnostics.healthcenter.api.cpu.CpuEvent;
import com.ibm.java.diagnostics.healthcenter.api.cpu.CpuEventListener;
import com.ibm.java.diagnostics.healthcenter.api.factory.HealthCenterFactory;
import com.ibm.java.diagnostics.healthcenter.api.gc.GCData;
import com.ibm.java.diagnostics.healthcenter.api.gc.GCEvent;
import com.ibm.java.diagnostics.healthcenter.api.gc.GCEventListener;
import com.ibm.java.diagnostics.healthcenter.api.io.FileEvent;
import com.ibm.java.diagnostics.healthcenter.api.io.FileEventListener;
import com.ibm.java.diagnostics.healthcenter.api.locking.LockingEvent;
import com.ibm.java.diagnostics.healthcenter.api.locking.LockingEventListener;
import com.ibm.java.diagnostics.healthcenter.api.methodtrace.MethodTraceData;
import com.ibm.java.diagnostics.healthcenter.api.nativememory.NativeMemoryCategoryEvent;
import com.ibm.java.diagnostics.healthcenter.api.nativememory.NativeMemoryCategoryEventListener;
import com.ibm.java.diagnostics.healthcenter.api.nativememory.NativeMemoryEvent;
import com.ibm.java.diagnostics.healthcenter.api.nativememory.NativeMemoryEventListener;
import com.ibm.java.diagnostics.healthcenter.api.profiling.MethodProfileData;
import com.ibm.java.diagnostics.healthcenter.api.profiling.ProfilingData;
import com.ibm.java.diagnostics.healthcenter.api.profiling.ProfilingEvent;
import com.ibm.java.diagnostics.healthcenter.api.profiling.ProfilingEventListener;
import com.ibm.java.diagnostics.healthcenter.api.profiling.ProfilingMethod;
import com.ibm.java.diagnostics.healthcenter.api.threads.ThreadEvent;
import com.ibm.java.diagnostics.healthcenter.api.threads.ThreadEventListener;
import com.ibm.java.diagnostics.healthcenter.api.threads.ThreadsData;

public class HCDProfiler {
	File hcdFile;
	HealthCenter hcAPI;
	Map<String, ProfilingData> hcdProfilingData = new HashMap<String, ProfilingData>();

	public HCDProfiler(File hcdFile) {
		this.hcdFile = hcdFile;

		// parse file using health center API
		HealthCenterFactory.setMemoryRestictionEnabled(false);
	}

	public List<HotMethod> analyseHCD(String documentPackage) {
		if (hcdFile != null && hcdFile.exists()) {
			try {
				List<HotMethod> hotMethods = analyseProfileInfo(documentPackage);
				return hotMethods;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("File " + hcdFile.getAbsolutePath()
					+ " does not exist");
		}

		return new ArrayList<HotMethod>();
	}

	private List<HotMethod> analyseProfileInfo(String documentPackage) throws HealthCenterException, FileNotFoundException {
		String[] recommendations;
		ProfilingData profilingData = this.hcdProfilingData.get(this.hcdFile.getAbsolutePath());

		if(profilingData == null) {
			this.hcAPI = HealthCenterFactory.connect(this.hcdFile);
			profilingData = this.hcAPI.getProfilingData();
			this.hcdProfilingData.put(this.hcdFile.getAbsolutePath(), profilingData);
		}

		if (profilingData == null) {
			System.out.println("no profiling data yet");
			return new ArrayList<HotMethod>();
		}

		recommendations = profilingData.getAllRecommendations();

		MethodProfileData[] mPD = profilingData.getProfilingEvents();
		if (mPD != null && mPD.length > 0) {
			boolean stabilised = false;
			List<HotMethod> hotMethods = new ArrayList<HotMethod>();

			System.out.println();
			System.out.println("Hot Methods for Package '" + documentPackage + "':");
			System.out.println("==========================================================");
			for (MethodProfileData methodProfileData : mPD) {
				System.out.println(methodProfileData.getMethodName());
				// only add methods from the current package
				if(methodProfileData.getMethodName().startsWith(documentPackage)) {
					// wait until the profiling has finished processing
					while(!stabilised) {
						try {
							Double percentageSample1 = methodProfileData.getMethodSamplePercentage();
							Thread.sleep(1000);
							Double percentageSample2 = methodProfileData.getMethodSamplePercentage();
							stabilised = percentageSample1.equals(percentageSample2);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					HotMethod hotMethod = new HotMethod(methodProfileData.getMethodName(), documentPackage, methodProfileData.getMethodSamplePercentage());

					hotMethods.add(hotMethod);

					System.out.println(hotMethod + "\n");
				}
			}
			this.hcAPI.endMonitoring();
			return hotMethods;
		}
		this.hcAPI.endMonitoring();
		return new ArrayList<HotMethod>();
	}

	private class SimpleProfileData implements Comparable<SimpleProfileData> {
		String methodName;
		int count;

		public int compareTo(SimpleProfileData compareMethod) {
			int compareCount = ((SimpleProfileData) compareMethod).getCount();
			return compareCount - this.count;

		}

		private int getCount() {
			return count;
		}

		private String getMethodName() {
			return methodName;
		}
	}
}
