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
package de.scoopgmbh.copper.monitoring.client.ui.systemresource.filter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import de.scoopgmbh.copper.monitoring.client.form.FxmlController;
import de.scoopgmbh.copper.monitoring.client.form.filter.BaseFilterController;
import de.scoopgmbh.copper.monitoring.client.form.filter.defaultfilter.DefaultFilterFactory;

public class ResourceFilterController extends BaseFilterController<ResourceFilterModel> implements Initializable, FxmlController {
	final ResourceFilterModel model= new ResourceFilterModel();

    @FXML //  fx:id="pane"
    private FlowPane pane; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert pane != null : "fx:id=\"pane\" was not injected: check your FXML file 'EngineLoadFilter.fxml'.";
        model.maxCountFilterModel.maxCount.set(50);
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
		return true;
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
