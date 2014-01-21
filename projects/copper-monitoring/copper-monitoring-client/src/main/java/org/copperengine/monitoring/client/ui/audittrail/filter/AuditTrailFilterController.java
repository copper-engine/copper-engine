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
package org.copperengine.monitoring.client.ui.audittrail.filter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import org.copperengine.monitoring.client.form.FxmlController;
import org.copperengine.monitoring.client.form.filter.BaseFilterController;
import org.copperengine.monitoring.client.form.filter.FilterController;
import org.copperengine.monitoring.client.form.filter.defaultfilter.DefaultFilterFactory;

public class AuditTrailFilterController extends BaseFilterController<AuditTrailFilterModel> implements Initializable, FxmlController {

    private final AuditTrailFilterModel model = new AuditTrailFilterModel();

    public AuditTrailFilterController() {
        super();
    }

    @FXML
    // fx:id="correlationId"
    private TextField correlationId; // Value injected by FXMLLoader

    @FXML
    // fx:id="level"
    private TextField level; // Value injected by FXMLLoader

    @FXML
    // fx:id="workflowClass"
    private TextField workflowClass; // Value injected by FXMLLoader

    @FXML
    // fx:id="workflowInstanceId"
    private TextField workflowInstanceId; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert correlationId != null : "fx:id=\"correlationId\" was not injected: check your FXML file 'AuditTrailFilter.fxml'.";
        assert level != null : "fx:id=\"level\" was not injected: check your FXML file 'AuditTrailFilter.fxml'.";
        assert workflowClass != null : "fx:id=\"workflowClass\" was not injected: check your FXML file 'AuditTrailFilter.fxml'.";
        assert workflowInstanceId != null : "fx:id=\"workflowInstanceId\" was not injected: check your FXML file 'AuditTrailFilter.fxml'.";

        workflowClass.textProperty().bindBidirectional(model.workflowClass);
        level.textProperty().bindBidirectional(model.level);
        correlationId.textProperty().bindBidirectional(model.correlationId);
        workflowInstanceId.textProperty().bindBidirectional(model.workflowInstanceId);
    }

    @Override
    public AuditTrailFilterModel getFilter() {
        return model;
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("AuditTrailFilter.fxml");
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
        return new DefaultFilterFactory().createMaxCount(model);
    }

}
