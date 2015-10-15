/*
 * Copyright 2002-2015 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.copperengine.monitoring.client.ui.dashboard.filter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import javafx.scene.Node;
import org.copperengine.monitoring.client.form.FxmlController;
import org.copperengine.monitoring.client.form.filter.BaseFilterController;
import org.copperengine.monitoring.client.form.filter.FilterController;
import org.copperengine.monitoring.client.form.filter.defaultfilter.DefaultFilterFactory;
import org.copperengine.monitoring.client.form.filter.defaultfilter.FromToMaxCountFilterModel;

public class DashboardFilterController extends BaseFilterController<FromToMaxCountFilterModel> implements Initializable, FxmlController {
    private final FromToMaxCountFilterModel model = new FromToMaxCountFilterModel();

    public DashboardFilterController() {
    }

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

    }

    @Override
    public FromToMaxCountFilterModel getFilter() {
        return model;
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("DashboardFilter.fxml");
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
        return new DefaultFilterFactory().createFromToMaxCount(model);
    }
}
