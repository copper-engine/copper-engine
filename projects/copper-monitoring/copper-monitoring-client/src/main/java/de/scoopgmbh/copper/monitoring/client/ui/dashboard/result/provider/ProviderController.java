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
package de.scoopgmbh.copper.monitoring.client.ui.dashboard.result.provider;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.FxmlController;
import de.scoopgmbh.copper.monitoring.core.model.MonitoringDataProviderInfo;

public class ProviderController implements Initializable, FxmlController {
	private final GuiCopperDataProvider dataProvider;
	private final MonitoringDataProviderInfo dataProviderInfo;
	

    public ProviderController(MonitoringDataProviderInfo dataProviderInfo, GuiCopperDataProvider  dataProvider) {
    	this.dataProvider = dataProvider;
    	this.dataProviderInfo = dataProviderInfo;
	}
    
	@Override
	public URL getFxmlResource() {
		return getClass().getResource("Provider.fxml");
	}

    @FXML //  fx:id="name"
    private TextField name; // Value injected by FXMLLoader

    @FXML //  fx:id="start"
    private Button start; // Value injected by FXMLLoader

    @FXML //  fx:id="status"
    private TextField status; // Value injected by FXMLLoader

    @FXML //  fx:id="stop"
    private Button stop; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert name != null : "fx:id=\"name\" was not injected: check your FXML file 'Provider.fxml'.";
        assert start != null : "fx:id=\"start\" was not injected: check your FXML file 'Provider.fxml'.";
        assert status != null : "fx:id=\"status\" was not injected: check your FXML file 'Provider.fxml'.";
        assert stop != null : "fx:id=\"stop\" was not injected: check your FXML file 'Provider.fxml'.";

        name.setText(dataProviderInfo.getName());
        status.setText(dataProviderInfo.getStatus());
        
        start.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				dataProvider.startMonitoringDataProvider(dataProviderInfo.getName());
			}
		});
        start.getStyleClass().add("copperActionButton");
        
        stop.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				dataProvider.stopMonitoringDataProvider(dataProviderInfo.getName());
			}
		});
        stop.getStyleClass().add("copperActionButton");
	}

}
