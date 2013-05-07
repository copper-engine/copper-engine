package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;
import java.util.Date;

public class AdapterWfLaunchInfo extends AdapterEventBase implements Serializable{
	private static final long serialVersionUID = 8349000202509066867L;
	
	String workflowname;
	Date timestamp;
	
	public AdapterWfLaunchInfo() {
		super();
	}

	public AdapterWfLaunchInfo(String workflowname, Date timestamp, String adapterName) {
		super(adapterName,timestamp);
		this.workflowname = workflowname;
		this.timestamp = timestamp;
	}

	public String getWorkflowname() {
		return workflowname;
	}

	public void setWorkflowname(String workflowname) {
		this.workflowname = workflowname;
	}
	
	
}
