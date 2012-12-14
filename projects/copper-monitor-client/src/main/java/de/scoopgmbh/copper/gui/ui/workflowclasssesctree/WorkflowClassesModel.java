package de.scoopgmbh.copper.gui.ui.workflowclasssesctree;

import javafx.beans.property.SimpleStringProperty;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowClassesInfo;

public class WorkflowClassesModel {
	public SimpleStringProperty classname;
	public SimpleStringProperty versionMajor;
	public SimpleStringProperty versionMinor;
	
	
	public WorkflowClassesModel(WorkflowClassesInfo workflowClassesInfo) {
		super();
		this.classname = new SimpleStringProperty(workflowClassesInfo.getClassname());
		this.versionMajor = new SimpleStringProperty(workflowClassesInfo.getVersionMajor());
		this.versionMinor = new SimpleStringProperty(workflowClassesInfo.getVersionMinor());
	}
	
	
}
