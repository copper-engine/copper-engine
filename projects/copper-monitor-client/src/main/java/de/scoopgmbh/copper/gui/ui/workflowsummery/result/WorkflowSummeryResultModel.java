package de.scoopgmbh.copper.gui.ui.workflowsummery.result;

import javafx.beans.property.SimpleStringProperty;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowSummery;

public class WorkflowSummeryResultModel {
	public SimpleStringProperty workflowclass;
	public SimpleStringProperty alias;
	public SimpleStringProperty workflowMajorVersion;
	public SimpleStringProperty workflowMinorVersion;
	public SimpleStringProperty status;
	public SimpleStringProperty count;
	
	public WorkflowSummeryResultModel(String  clazz, String alias, String workflowMajorVersion,
			String workflowMinorVersion, String status, String count) {
		super();
		this.workflowclass = new SimpleStringProperty(clazz);
		this.alias = new SimpleStringProperty(alias);
		this.workflowMajorVersion = new SimpleStringProperty(workflowMajorVersion);
		this.workflowMinorVersion = new SimpleStringProperty(workflowMinorVersion);
		this.status = new SimpleStringProperty(status);
		this.count = new SimpleStringProperty(count);
	}

	public WorkflowSummeryResultModel(WorkflowSummery workflowSummery) {
		this.workflowclass = new SimpleStringProperty(workflowSummery.getClazz());
		this.alias = new SimpleStringProperty(workflowSummery.getAlias());
		this.workflowMajorVersion = new SimpleStringProperty(workflowSummery.getWorkflowMajorVersion());
		this.workflowMinorVersion = new SimpleStringProperty(workflowSummery.getWorkflowMinorVersion());
		this.status = new SimpleStringProperty(workflowSummery.getStatus());
		this.count = new SimpleStringProperty(String.valueOf(workflowSummery.getCount()));
	}
	
	
	
	
}
