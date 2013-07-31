package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;

public class MonitoringDataProviderInfo implements Serializable{
	private static final long serialVersionUID = 6763592642428111845L;
	String status;
	String name;
	public MonitoringDataProviderInfo(String name, String status) {
		super();
		this.status = status;
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public MonitoringDataProviderInfo() {
		super();
	}

}
