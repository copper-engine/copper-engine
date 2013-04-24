/*
 * Copyright 2002-2013 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;
import java.util.Map;

public class WorkflowStateSummary implements Serializable {
	private static final long serialVersionUID = -5328848786752720825L;
	
	private Map<WorkflowInstanceState,Integer> numberOfWorkflowInstancesWithState;

	public Map<WorkflowInstanceState, Integer> getNumberOfWorkflowInstancesWithState() {
		return numberOfWorkflowInstancesWithState;
	}

	public void setNumberOfWorkflowInstancesWithState(Map<WorkflowInstanceState, Integer> numberOfWorkflowInstancesWithState) {
		this.numberOfWorkflowInstancesWithState = numberOfWorkflowInstancesWithState;
	}

	public WorkflowStateSummary(Map<WorkflowInstanceState, Integer> numberOfWorkflowInstancesWithState) {
		super();
		this.numberOfWorkflowInstancesWithState = numberOfWorkflowInstancesWithState;
	}

	@Override
	public String toString() {
		return "WorkflowStateSummary [numberOfWorkflowInstancesWithState=" + numberOfWorkflowInstancesWithState + "]";
	}
	
	public int getCount(WorkflowInstanceState workflowInstanceState){
		Integer count = numberOfWorkflowInstancesWithState.get(workflowInstanceState);
		if (count == null){
			return 0;
		} else {
			return count;
		}
	}
	
	
	
}
