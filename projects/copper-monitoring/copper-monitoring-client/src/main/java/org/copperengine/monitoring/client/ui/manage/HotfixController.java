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
package org.copperengine.monitoring.client.ui.manage;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import org.copperengine.monitoring.client.adapter.GuiCopperDataProvider;
import org.copperengine.monitoring.client.form.FxmlController;
import org.copperengine.monitoring.client.form.filter.enginefilter.EngineSelectionWidget;

public class HotfixController implements Initializable, FxmlController {
    private final HotfixModel hotfixModelModel;
    private final GuiCopperDataProvider copperDataProvider;

    public HotfixController(HotfixModel hotfixModelModel, GuiCopperDataProvider copperDataProvider) {
        super();
        this.hotfixModelModel = hotfixModelModel;
        this.copperDataProvider = copperDataProvider;
    }

    @FXML
    // fx:id="pane"
    private HBox pane; // Value injected by FXMLLoader

    @FXML
    // fx:id="restartAll"
    private Button restartAll; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert pane != null : "fx:id=\"pane\" was not injected: check your FXML file 'Hotfix.fxml'.";
        assert restartAll != null : "fx:id=\"restartAll\" was not injected: check your FXML file 'Hotfix.fxml'.";

        restartAll.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                copperDataProvider.restartAllError(hotfixModelModel.selectedEngine.get().getId());
            }
        });
        restartAll.getStyleClass().add("copperActionButton");

        Node engineSelectionWidget = new EngineSelectionWidget(hotfixModelModel, copperDataProvider.getEngineList()).createContent();
        HBox.setMargin(engineSelectionWidget, new Insets(3));
        pane.getChildren().add(engineSelectionWidget);
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("Hotfix.fxml");
    }
}
