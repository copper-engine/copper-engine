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
package org.copperengine.monitoring.client.ui.configuration.result.pool;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import org.copperengine.monitoring.client.adapter.GuiCopperDataProvider;
import org.copperengine.monitoring.client.context.FormContext;
import org.copperengine.monitoring.client.form.FxmlController;
import org.copperengine.monitoring.client.form.dialog.DefaultInputDialogCreator;
import org.copperengine.monitoring.client.form.dialog.InputDialogCreator;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo;
import org.copperengine.monitoring.core.model.ProcessorPoolInfo;

public class ProcessorPoolController implements Initializable, FxmlController {
    private ProcessorPoolInfo pool;
    private final ProcessingEngineInfo engine;
    private final FormContext context;
    private final GuiCopperDataProvider dataProvider;
    private final InputDialogCreator inputDialogCreator;

    public ProcessorPoolController(ProcessingEngineInfo engine, ProcessorPoolInfo pool, FormContext context, GuiCopperDataProvider dataProvider, InputDialogCreator inputDialogCreator) {
        this.pool = pool;
        this.engine = engine;
        this.context = context;
        this.dataProvider = dataProvider;
        this.inputDialogCreator = inputDialogCreator;
    }

    @FXML
    // fx:id="id"
    private TextField id; // Value injected by FXMLLoader

    @FXML
    // fx:id="numberButton"
    private Button numberButton; // Value injected by FXMLLoader

    @FXML
    // fx:id="prioButton"
    private Button prioButton; // Value injected by FXMLLoader

    @FXML
    // fx:id="threadNumberInfo"
    private TextField threadNumberInfo; // Value injected by FXMLLoader

    @FXML
    // fx:id="threadPrioritaetInfo"
    private TextField threadPrioritaetInfo; // Value injected by FXMLLoader

    @FXML
    // fx:id="typ"
    private TextField typ; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert id != null : "fx:id=\"id\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
        assert numberButton != null : "fx:id=\"numberButton\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
        assert prioButton != null : "fx:id=\"prioButton\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
        assert threadNumberInfo != null : "fx:id=\"threadNumberInfo\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
        assert threadPrioritaetInfo != null : "fx:id=\"threadPrioritaetInfo\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
        assert typ != null : "fx:id=\"typ\" was not injected: check your FXML file 'ProcessorPool.fxml'.";

        updatePool();

        prioButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                inputDialogCreator.showIntInputDialog("Thread priority", pool.getThreadPriority(), new DefaultInputDialogCreator.DialogClosed<Integer>() {
                    @Override
                    public void closed(Integer inputValue) {
                        dataProvider.setThreadPriority(engine.getId(), pool.getId(), inputValue);
                        context.createConfigurationForm().delayedRefresh();
                    }
                });
            }
        });

        numberButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                inputDialogCreator.showIntInputDialog("Thread count", pool.getNumberOfThreads(), new DefaultInputDialogCreator.DialogClosed<Integer>() {
                    @Override
                    public void closed(Integer inputValue) {
                        dataProvider.setNumberOfThreads(engine.getId(), pool.getId(), inputValue);
                        context.createConfigurationForm().delayedRefresh();
                    }
                });
            }
        });
    }

    public void setPool(ProcessorPoolInfo pool) {
        this.pool = pool;
        updatePool();
    }

    private void updatePool() {
        id.setText(pool.getId());
        typ.setText(pool.getProcessorPoolTyp().toString());

        threadNumberInfo.setText(String.valueOf(pool.getNumberOfThreads()));
        threadPrioritaetInfo.setText(String.valueOf(pool.getThreadPriority()));
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("ProcessorPool.fxml");
    }

}
