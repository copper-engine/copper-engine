package de.scoopgmbh.copper.monitor.adapter.model;

import java.io.Serializable;

public class CopperLoadInfo implements Serializable {
	private static final long serialVersionUID = -5328848786752720825L;
	
	public int numberOfRunningWorkflowInstances;
	public int numberOfWaitingWorkflowInstances;
	
	public CopperLoadInfo(int numberOfRunningWorkflowInstances, int numberOfWaitingWorkflowInstances) {
		super();
		this.numberOfRunningWorkflowInstances = numberOfRunningWorkflowInstances;
		this.numberOfWaitingWorkflowInstances = numberOfWaitingWorkflowInstances;
	}
	public int getNumberOfRunningWorkflowInstances() {
		return numberOfRunningWorkflowInstances;
	}
	public void setNumberOfRunningWorkflowInstances(int numberOfRunningWorkflowInstances) {
		this.numberOfRunningWorkflowInstances = numberOfRunningWorkflowInstances;
	}
	public int getNumberOfWaitingWorkflowInstances() {
		return numberOfWaitingWorkflowInstances;
	}
	public void setNumberOfWaitingWorkflowInstances(int numberOfWaitingWorkflowInstances) {
		this.numberOfWaitingWorkflowInstances = numberOfWaitingWorkflowInstances;
	}

	
}
