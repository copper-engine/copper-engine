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
package de.scoopgmbh.copper.monitoring.client.ui.worklowinstancedetail.filter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import de.scoopgmbh.copper.monitoring.client.form.FxmlController;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterController;

public class WorkflowInstanceDetailFilterController implements Initializable, FilterController<WorkflowInstanceDetailFilterModel>, FxmlController {
	private WorkflowInstanceDetailFilterModel model;

	public WorkflowInstanceDetailFilterController(String workflowInstanceId) {
		super();
		model = new WorkflowInstanceDetailFilterModel();
		model.workflowInstanceId.setValue(workflowInstanceId);
	}
	
	
	public void setFilter(String workflowInstanceId){
		model.workflowInstanceId.setValue(workflowInstanceId);
	}

    @FXML //  fx:id="workflowInstanceIdTextfield"
    private TextField workflowInstanceIdTextfield; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert workflowInstanceIdTextfield != null : "fx:id=\"workflowInstanceIdTextfield\" was not injected: check your FXML file 'WorkflowInstanceDetailFilter.fxml'.";

        workflowInstanceIdTextfield.textProperty().bindBidirectional(model.workflowInstanceId);
	}

	@Override
	public WorkflowInstanceDetailFilterModel getFilter() {
		return model;
	}

	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("WorkflowInstanceDetailFilter.fxml");
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
