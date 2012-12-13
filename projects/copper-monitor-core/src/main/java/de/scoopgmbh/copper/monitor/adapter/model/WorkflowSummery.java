package de.scoopgmbh.copper.monitor.adapter.model;

import java.io.Serializable;

public class WorkflowSummery implements Serializable {
	private static final long serialVersionUID = 4867510351238162279L;
	
	private String clazz;
	private String alias;
	private String workflowMajorVersion;
	private String workflowMinorVersion;
	private String status;
	private int count;
	
	public WorkflowSummery() {
		super();
	}
	
	public WorkflowSummery(String clazz, String alias, String workflowMajorVersion, String workflowMinorVersion, String status, int count) {
		super();
		this.clazz = clazz;
		this.alias = alias;
		this.workflowMajorVersion = workflowMajorVersion;
		this.workflowMinorVersion = workflowMinorVersion;
		this.status = status;
		this.count = count;
	}
	public String getClazz() {
		return clazz;
	}
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getWorkflowMajorVersion() {
		return workflowMajorVersion;
	}
	public void setWorkflowMajorVersion(String workflowMajorVersion) {
		this.workflowMajorVersion = workflowMajorVersion;
	}
	public String getWorkflowMinorVersion() {
		return workflowMinorVersion;
	}
	public void setWorkflowMinorVersion(String workflowMinorVersion) {
		this.workflowMinorVersion = workflowMinorVersion;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	
	
	
}
