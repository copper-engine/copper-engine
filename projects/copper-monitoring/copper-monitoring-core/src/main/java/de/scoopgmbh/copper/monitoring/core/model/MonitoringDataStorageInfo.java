package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class MonitoringDataStorageInfo implements Serializable{
	private static final long serialVersionUID = 2363074938576106353L;
	private double sizeInMb;
	private String path;
	private HashMap<String,Long> classToCount = new HashMap<String,Long>();
	private Date min;
	private Date max;

	public MonitoringDataStorageInfo(double sizeInMb, String path, HashMap<String, Long> classToCount, Date min, Date max) {
		super();
		this.sizeInMb = sizeInMb;
		this.path = path;
		this.classToCount = classToCount;
		this.min = min;
		this.max = max;
	}
	public MonitoringDataStorageInfo() {
		super();
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
	public Date getMin() {
		return min;
	}
	public void setMin(Date min) {
		this.min = min;
	}
	public Date getMax() {
		return max;
	}
	public void setMax(Date max) {
		this.max = max;
	}
	

}
