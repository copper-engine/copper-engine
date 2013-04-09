package de.scoopgmbh.copper.monitor.adapter.model;

import java.io.Serializable;
import java.util.List;

public class WorkflowRepositoryInfo implements Serializable{
	private static final long serialVersionUID = 5658665409495683627L;
	
	public static enum WorkflowRepositorTyp{
		FILE
	}
	
	WorkflowRepositorTyp workflowRepositorTyp;
	String name;
	List<String> srcPaths;
	
	
	public WorkflowRepositoryInfo() {
		super();
	}

	public WorkflowRepositoryInfo(WorkflowRepositorTyp workflowRepositorTyp, String name, List<String> srcPaths) {
		super();
		this.workflowRepositorTyp = workflowRepositorTyp;
		this.name = name;
		this.srcPaths = srcPaths;
	}
	
	public WorkflowRepositorTyp getWorkflowRepositorTyp() {
		return workflowRepositorTyp;
	}

	public void setWorkflowRepositorTyp(WorkflowRepositorTyp workflowRepositorTyp) {
		this.workflowRepositorTyp = workflowRepositorTyp;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getSrcPaths() {
		return srcPaths;
	}
	public void setSrcPaths(List<String> srcPaths) {
		this.srcPaths = srcPaths;
	}

}
