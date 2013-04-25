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
package de.scoopgmbh.copper.monitoring.client.ui.workflowhistory.filter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import de.scoopgmbh.copper.monitoring.client.form.FxmlController;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterController;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceState;

public class WorkflowHistoryFilterController implements Initializable, FilterController<WorkflowHistoryFilterModel>, FxmlController {
	WorkflowHistoryFilterModel model = new WorkflowHistoryFilterModel();

	public class EmptySelectionWorkaround{
		public WorkflowInstanceState value;
		public String text;
		public EmptySelectionWorkaround(WorkflowInstanceState value, String text) {
			super();
			this.value = value;
			this.text = text;
		}
		
	}


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
       
	}

	@Override
	public WorkflowHistoryFilterModel getFilter() {
		return model;
	}

	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("WorkflowHistoryFilter.fxml");
	}
	
	@Override
	public boolean supportsFiltering() {
		return true;
	}
	
	@Override
	public long getDefaultRefreshIntervall() {
		return FilterController.DEFAULT_REFRESH_INTERVALL;
	}
	
}
