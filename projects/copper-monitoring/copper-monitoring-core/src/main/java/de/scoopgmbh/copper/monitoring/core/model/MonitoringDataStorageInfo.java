package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;
import java.util.HashMap;

public class MonitoringDataStorageInfo implements Serializable{
	private static final long serialVersionUID = 2363074938576106353L;
	private double sizeInMb;
	private String path;
	private HashMap<String,Long> classToCount = new HashMap<String,Long>();
	public MonitoringDataStorageInfo(double sizeInMb, String path, HashMap<String, Long> classToCount) {
		super();
		this.sizeInMb = sizeInMb;
		this.path = path;
		this.classToCount = classToCount;
	}
	public double getSizeInMb() {
		return sizeInMb;
	}
	public void setSizeInMb(double sizeInMb) {
		this.sizeInMb = sizeInMb;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public HashMap<String, Long> getClassToCount() {
		return classToCount;
	}
	public void setClassToCount(HashMap<String, Long> classToCount) {
		this.classToCount = classToCount;
	}
	public MonitoringDataStorageInfo() {
		super();
	}
	

}
