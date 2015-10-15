/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.monitoring.client.ui.systemresource.filter;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;

import org.copperengine.monitoring.client.form.FxmlController;
import org.copperengine.monitoring.client.form.filter.BaseFilterController;
import org.copperengine.monitoring.client.form.filter.defaultfilter.DefaultFilterFactory;

public class ResourceFilterController extends BaseFilterController<ResourceFilterModel> implements Initializable, FxmlController {
    private final ResourceFilterModel model = new ResourceFilterModel();
    
    private final int initMaxCount;
    private final Date initFrom;
    private final Date initTo;

    @FXML
    // fx:id="pane"
    private FlowPane pane; // Value injected by FXMLLoader


    public ResourceFilterController() {
        this(-1, null, null);
    }

    public ResourceFilterController(int initMaxCount, Date initFrom, Date initTo) {
        this.initMaxCount = (initMaxCount <= 0) ? 50 : initMaxCount;
        this.initFrom = initFrom;
        this.initTo = initTo;
    }
    
    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert pane != null : "fx:id=\"pane\" was not injected: check your FXML file 'EngineLoadFilter.fxml'.";
        model.maxCountFilterModel.maxCount.set(initMaxCount);
        if(initFrom != null) {
            model.fromToFilterModel.from.set(initFrom);
        }
        if(initTo != null) {
            model.fromToFilterModel.to.set(initTo);
        }
    }

    @Override
    public ResourceFilterModel getFilter() {
        return model;
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("ResourceFilter.fxml");
    }

    @Override
    public boolean supportsFiltering() {
        return false;
    }

    @Override
    public long getDefaultRefreshInterval() {
        return 1500;
    }

    @Override
    public Node createDefaultFilter() {
        return new DefaultFilterFactory().createFromToMaxCount(model);
    }
}
