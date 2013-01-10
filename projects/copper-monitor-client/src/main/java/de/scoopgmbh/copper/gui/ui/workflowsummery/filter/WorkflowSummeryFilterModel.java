package de.scoopgmbh.copper.gui.ui.workflowsummery.filter;

import javafx.beans.property.SimpleStringProperty;
import de.scoopgmbh.copper.gui.ui.workflowsummery.result.WorkflowSummeryResultModel;

public class WorkflowSummeryFilterModel {
	public SimpleStringProperty workflowclass = new SimpleStringProperty();
	public SimpleStringProperty workflowMajorVersion = new SimpleStringProperty();
	public SimpleStringProperty workflowMinorVersion = new SimpleStringProperty();
	
	public void setAllFrom(WorkflowSummeryResultModel workflowSummeryResultModel){
		workflowclass.set(workflowSummeryResultModel.workflowclass.get());
		workflowMajorVersion.set(workflowSummeryResultModel.workflowMajorVersion.get());
		workflowMinorVersion.set(workflowSummeryResultModel.workflowMinorVersion.get());
	}
}
