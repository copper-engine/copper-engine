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
package de.scoopgmbh.copper.gui.ui.workflowsummery.result;

import javafx.beans.property.SimpleStringProperty;
import de.scoopgmbh.copper.gui.util.WorkflowVersion;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowSummery;

public class WorkflowSummeryResultModel {
	public final SimpleStringProperty alias;
	public final WorkflowVersion version;
	public final SimpleStringProperty status;
	public final SimpleStringProperty count;

	public WorkflowSummeryResultModel(WorkflowSummery workflowSummery) {
		this.alias = new SimpleStringProperty(workflowSummery.getAlias());
		
		this.version = new WorkflowVersion(
				workflowSummery.getWorkflowClass(),
				workflowSummery.getWorkflowMajorVersion(),
				workflowSummery.getWorkflowMinorVersion(), 
				workflowSummery.getWorkflowPatchLevel());
		this.status = new SimpleStringProperty(workflowSummery.getStatus());
		this.count = new SimpleStringProperty(String.valueOf(workflowSummery.getCount()));
	}
	
	
	
	
}
