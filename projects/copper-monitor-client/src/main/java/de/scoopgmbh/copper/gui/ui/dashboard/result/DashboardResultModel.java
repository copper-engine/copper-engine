/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.gui.ui.dashboard.result;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.SimpleIntegerProperty;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowStateSummery;

public class DashboardResultModel {
	
	private Map<WorkflowInstanceState,SimpleIntegerProperty> stateOverview = new HashMap<>();
	
	public DashboardResultModel(WorkflowStateSummery stateSummery){
		for (WorkflowInstanceState workflowInstanceState: WorkflowInstanceState.values()){
			stateOverview.put(workflowInstanceState, new SimpleIntegerProperty());
		}
		
		update(stateSummery);
	}
	
	public void update(WorkflowStateSummery stateSummery){
		for (WorkflowInstanceState workflowInstanceState: WorkflowInstanceState.values()){
			stateOverview.get(workflowInstanceState).setValue(stateSummery.getNumberOfWorkflowInstancesWithState().get(workflowInstanceState));
		}
	}
}
