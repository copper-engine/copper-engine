/*
 * Copyright 2002-2013 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.scoopgmbh.copper.monitor.core.adapter.model;

import java.io.Serializable;
import java.util.Date;

public class SystemResourcesInfo implements Serializable{
	private static final long serialVersionUID = 1248127727507816766L;
	
	Date timestamp;
	double systemCpuLoad;
	long freePhysicalMemorySize;
	double processCpuLoad;
	double heapMemoryUsage;
	long liveThreadsCount;
	long totalLoadedClassCount;
	
	public SystemResourcesInfo(Date timestamp, double systemCpuLoad, long freePhysicalMemorySize, double processCpuLoad, 
			double heapMemoryUsage, long liveThreadsCount, long totalLoadedClassCount) {
		super();
		this.timestamp = timestamp;
		this.systemCpuLoad = systemCpuLoad;
		this.freePhysicalMemorySize = freePhysicalMemorySize;
		this.processCpuLoad = processCpuLoad;
		this.heapMemoryUsage = heapMemoryUsage;
		this.liveThreadsCount = liveThreadsCount;
		this.totalLoadedClassCount = totalLoadedClassCount;
	}

	public long getLiveThreadsCount() {
		return liveThreadsCount;
	}

	public void setLiveThreadsCount(long liveThreadsCount) {
		this.liveThreadsCount = liveThreadsCount;
	}

	public long getTotalLoadedClassCount() {
		return totalLoadedClassCount;
	}

	public void setTotalLoadedClassCount(long totalLoadedClassCount) {
		this.totalLoadedClassCount = totalLoadedClassCount;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public double getSystemCpuLoad() {
		return systemCpuLoad;
	}

	public void setSystemCpuLoad(double systemCpuLoad) {
		this.systemCpuLoad = systemCpuLoad;
	}

	public long getFreePhysicalMemorySize() {
		return freePhysicalMemorySize;
	}

	public void setFreePhysicalMemorySize(long freePhysicalMemorySize) {
		this.freePhysicalMemorySize = freePhysicalMemorySize;
	}

	public double getProcessCpuLoad() {
		return processCpuLoad;
	}

	public void setProcessCpuLoad(double processCpuLoad) {
		this.processCpuLoad = processCpuLoad;
	}

	public double getHeapMemoryUsage() {
		return heapMemoryUsage;
	}

	public void setHeapMemoryUsage(double heapMemoryUsage) {
		this.heapMemoryUsage = heapMemoryUsage;
	}
	
}
