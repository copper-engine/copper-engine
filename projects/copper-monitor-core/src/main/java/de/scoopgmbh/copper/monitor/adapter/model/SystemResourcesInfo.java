package de.scoopgmbh.copper.monitor.adapter.model;

import java.io.Serializable;
import java.util.Date;

public class SystemResourcesInfo implements Serializable{
	private static final long serialVersionUID = 1248127727507816766L;
	
	Date timestamp;
	double systemCpuLoad;
	long freePhysicalMemorySize;
	double processCpuLoad;
	
	public SystemResourcesInfo(Date timestamp, double systemCpuLoad, long freePhysicalMemorySize, double processCpuLoad) {
		super();
		this.timestamp = timestamp;
		this.systemCpuLoad = systemCpuLoad;
		this.freePhysicalMemorySize = freePhysicalMemorySize;
		this.processCpuLoad = processCpuLoad;
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
	
	
	

}
