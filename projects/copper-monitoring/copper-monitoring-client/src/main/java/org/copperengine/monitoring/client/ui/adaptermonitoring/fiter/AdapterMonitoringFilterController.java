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
package org.copperengine.monitoring.client.ui.adaptermonitoring.fiter;

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

public class AdapterMonitoringFilterController extends BaseFilterController<AdapterMonitoringFilterModel> implements Initializable, FxmlController {
    private final AdapterMonitoringFilterModel model = new AdapterMonitoringFilterModel();

    public AdapterMonitoringFilterController() {
        super();
    }

    @FXML
    // fx:id="instanceId"
    private TextField instanceId; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert instanceId != null : "fx:id=\"instanceId\" was not injected: check your FXML file 'AdapterMonitoringFilter.fxml'.";

        model.adapterId.bindBidirectional(instanceId.textProperty());
    }

    @Override
    public AdapterMonitoringFilterModel getFilter() {
        return model;
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("AdapterMonitoringFilter.fxml");
    }

    @Override
    public boolean supportsFiltering() {
        return true;
    }

    @Override
    public long getDefaultRefreshInterval() {
        return FilterController.DEFAULT_REFRESH_INTERVALL;
    }

    @Override
    public Node createDefaultFilter() {
        return new DefaultFilterFactory().createFromToMaxCount(model);
    }

}
