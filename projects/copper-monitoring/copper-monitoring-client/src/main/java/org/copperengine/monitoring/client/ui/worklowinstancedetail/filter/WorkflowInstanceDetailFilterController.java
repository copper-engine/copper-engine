/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.monitoring.client.ui.worklowinstancedetail.filter;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import org.copperengine.monitoring.client.form.FxmlController;
import org.copperengine.monitoring.client.form.filter.FilterController;
import org.copperengine.monitoring.client.form.filter.enginefilter.BaseEngineFilterController;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo;

public class WorkflowInstanceDetailFilterController extends BaseEngineFilterController<WorkflowInstanceDetailFilterModel> implements Initializable,
        FxmlController {

    public WorkflowInstanceDetailFilterController(WorkflowInstanceDetailFilterModel model, List<ProcessingEngineInfo> availableEngines) {
        super(availableEngines, model);
    }

    public void setFilter(String workflowInstanceId) {
        model.workflowInstanceId.setValue(workflowInstanceId);
    }

    @FXML
    // fx:id="workflowInstanceIdTextfield"
    private TextField workflowInstanceIdTextfield; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert workflowInstanceIdTextfield != null : "fx:id=\"workflowInstanceIdTextfield\" was not injected: check your FXML file 'WorkflowInstanceDetailFilter.fxml'.";

        workflowInstanceIdTextfield.textProperty().bindBidirectional(model.workflowInstanceId);
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("WorkflowInstanceDetailFilter.fxml");
    }

    @Override
    public boolean supportsFiltering() {
        return true;
    }

    @Override
    public long getDefaultRefreshInterval() {
        return FilterController.DEFAULT_REFRESH_INTERVAL;
    }

    @Override
    public Node createDefaultFilter() {
        return null;
    }

    @Override
    public Node createAdditionalFilter() {
        return null;
    }

}
