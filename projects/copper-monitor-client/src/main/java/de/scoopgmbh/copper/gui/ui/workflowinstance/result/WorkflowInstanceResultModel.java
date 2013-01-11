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
package de.scoopgmbh.copper.gui.ui.workflowinstance.result;

import java.util.Date;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;

public class WorkflowInstanceResultModel {
	public SimpleStringProperty id;
	public SimpleObjectProperty<WorkflowInstanceState> state;
	public SimpleIntegerProperty priority;
	public SimpleStringProperty processorPoolId;
	public SimpleObjectProperty<Date> timeout;

	public WorkflowInstanceResultModel(WorkflowInstanceInfo workflowInstanceInfo) {
		this.id = new SimpleStringProperty(workflowInstanceInfo.getId());
		this.state = new SimpleObjectProperty<>(workflowInstanceInfo.getState());
		this.priority = new SimpleIntegerProperty(workflowInstanceInfo.getPriority());
		this.processorPoolId = new SimpleStringProperty(workflowInstanceInfo.getProcessorPoolId());
		this.timeout = new SimpleObjectProperty<>(workflowInstanceInfo.getTimeout());
	}
	
	
	
	
}
