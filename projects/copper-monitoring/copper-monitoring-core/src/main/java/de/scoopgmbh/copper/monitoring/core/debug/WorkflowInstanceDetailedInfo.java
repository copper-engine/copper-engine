package de.scoopgmbh.copper.monitoring.core.debug;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class WorkflowInstanceDetailedInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;

	String workflowInstanceId;
	List<StackFrame> stack;

	public String getWorkflowInstanceId() {
		return workflowInstanceId;
	}

	public List<StackFrame> getStack() {
		return Collections.unmodifiableList(stack);
	}

	public WorkflowInstanceDetailedInfo(String workflowInstanceId, List<StackFrame> stack) {
		this.workflowInstanceId = workflowInstanceId;
		this.stack = stack;
	}

}
