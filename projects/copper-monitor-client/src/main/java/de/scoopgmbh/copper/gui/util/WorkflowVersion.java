package de.scoopgmbh.copper.gui.util;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowClassesInfo;

public class WorkflowVersion {
	public final SimpleStringProperty classname = new SimpleStringProperty(); 
	public final SimpleObjectProperty<Long> versionMajor = new SimpleObjectProperty<>(); 
	public final SimpleObjectProperty<Long> versionMinor = new SimpleObjectProperty<>(); 
	public final SimpleObjectProperty<Long> patchlevel = new SimpleObjectProperty<>(); 
	
	public WorkflowVersion(){

	} 
	
	public WorkflowVersion(String classname, long versionMajor,long versionMinor, long patchlevel){
		this.classname.setValue(classname);
		this.versionMajor.setValue(versionMajor);
		this.versionMinor.setValue(versionMinor);
		this.patchlevel.setValue(patchlevel);
	}
	
	public WorkflowVersion(WorkflowClassesInfo workflowClassesInfo){
		this.classname.setValue(workflowClassesInfo.getClassname());
		this.versionMajor.setValue(workflowClassesInfo.getMajorVersion());
		this.versionMinor.setValue(workflowClassesInfo.getMinorVersion());
		this.patchlevel.setValue(workflowClassesInfo.getPatchLevel());
	}
	
	public void setAllFrom(WorkflowVersion workflowVersion){
		classname.set(workflowVersion.classname.get());
		versionMajor.set(workflowVersion.versionMajor.get());
		versionMinor.set(workflowVersion.versionMinor.get());
		patchlevel.set(workflowVersion.patchlevel.get());
	}
	
}
