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
package de.scoopgmbh.copper.gui.ui.load.filter;

import java.net.URL;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterController;
import de.scoopgmbh.copper.monitor.core.adapter.model.WorkflowInstanceState;

public class EngineLoadFilterController implements Initializable, FilterController<EngineLoadFilterModel>, FxmlController {
	final EngineLoadFilterModel model= new EngineLoadFilterModel();



    @FXML //  fx:id="pane"
    private HBox pane; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert pane != null : "fx:id=\"pane\" was not injected: check your FXML file 'EngineLoadFilter.fxml'.";

        for (Entry<WorkflowInstanceState,SimpleBooleanProperty> entry: model.stateFilters.entrySet()){
        	CheckBox checkBox  = new CheckBox();
        	checkBox.setText(entry.getKey().toString());
        	checkBox.selectedProperty().bindBidirectional(entry.getValue());
        	pane.getChildren().add(checkBox);
        }
	}

	@Override
	public EngineLoadFilterModel getFilter() {
		return model;
	}

	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("EngineLoadFilter.fxml");
	}
	
	@Override
	public boolean supportsFiltering() {
		return true;
	}
	
	
}
