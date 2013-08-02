package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;

public class MonitoringDataStorageContentInfo implements Serializable{
	private static final long serialVersionUID = -6992686678495072759L;
	private String type;
	private long count;
	
	public MonitoringDataStorageContentInfo(String type, long count) {
		super();
		this.type = type;
		this.count = count;
	}
	public MonitoringDataStorageContentInfo() {
		super();
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}


}
