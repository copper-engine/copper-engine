package de.scoopgmbh.copper.monitor.adapter.model;

import java.io.Serializable;

public class WorkflowInstanceHistory implements Serializable{
	private static final long serialVersionUID = -7316596553420665952L;
	
	long timestamp;
	String stateName;
	String instanceId;
	String classname;
	
	public WorkflowInstanceHistory(long timestamp, String stateName, String instanceId, String classname) {
		super();
		this.timestamp = timestamp;
		this.stateName = stateName;
		this.instanceId = instanceId;
		this.classname = classname;
	}
	
	public WorkflowInstanceHistory() {
		super();
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public String getClassname() {
		return classname;
	}
	public void setClassname(String classname) {
		this.classname = classname;
	}
	
	
}
