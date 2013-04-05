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
package de.scoopgmbh.copper.persistent;

import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;

enum DBProcessingState {
	ENQUEUED(0), 
	PROCESSING(1) /* so far unused */, 
	WAITING(2), 
	FINISHED(3), 
	INVALID(4), 
	ERROR(5);
	
	static final DBProcessingState[] states = new DBProcessingState[values().length];
	static {
		for (DBProcessingState s : values()) {
			if (states[s.key()] != null)
				throw new RuntimeException("Inconsistent key mapping found for "+s);
			states[s.key()] = s;
			//check 1 to 1 mapping between DBProcessingState and WorkflowInstanceState
			WorkflowInstanceState.valueOf(s.name());
		}
		//check 1 to 1 mapping between DBProcessingState and WorkflowInstanceState
		for (WorkflowInstanceState s : WorkflowInstanceState.values()) {
			DBProcessingState.valueOf(s.name());
		}
	}
	
	final int key;
	DBProcessingState(int key) {
		this.key = key;
	}
	public int key() {
		return key;
	}
	public WorkflowInstanceState asWorkflowInstanceState() {
		return WorkflowInstanceState.valueOf(name());
	}
	
	public static DBProcessingState fromKey(int key) {
		DBProcessingState state = states[key];
		if (state == null)
			throw new IllegalArgumentException("No value for "+key);
		return state;
	}
	
	public static DBProcessingState fromWorkflowInstanceState(WorkflowInstanceState workflowInstanceState) {
		return DBProcessingState.valueOf(workflowInstanceState.name());
	}
	
}
