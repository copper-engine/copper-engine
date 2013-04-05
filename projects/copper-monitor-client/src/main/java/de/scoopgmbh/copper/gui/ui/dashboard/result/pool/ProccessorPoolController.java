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
package de.scoopgmbh.copper.gui.ui.dashboard.result.pool;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.ui.dashboard.result.DashboardResultModel;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessorPoolInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowStateSummary;

public class ProccessorPoolController implements Initializable, FxmlController {
	private final ProcessorPoolInfo pool;
	private final ProcessingEngineInfo engine;
	private final DashboardResultModel model;
	private final GuiCopperDataProvider dataProvider;

    public ProccessorPoolController(ProcessingEngineInfo engine, ProcessorPoolInfo pool, DashboardResultModel model, GuiCopperDataProvider  dataProvider) {
    	this.pool=pool;
    	this.model=model;
    	this.engine = engine;
    	this.dataProvider = dataProvider;
	}


    @FXML //  fx:id="dequeued"
    private TextField dequeued; // Value injected by FXMLLoader

    @FXML //  fx:id="enqueued"
    private TextField enqueued; // Value injected by FXMLLoader

    @FXML //  fx:id="id"
    private TextField id; // Value injected by FXMLLoader

    @FXML //  fx:id="numemrbutton"
    private Button nummerbutton; // Value injected by FXMLLoader

    @FXML //  fx:id="nummerNew"
    private TextField nummerNew; // Value injected by FXMLLoader

    @FXML //  fx:id="prioButton"
    private Button prioButton; // Value injected by FXMLLoader

    @FXML //  fx:id="prioNew"
    private TextField prioNew; // Value injected by FXMLLoader

    @FXML //  fx:id="running"
    private TextField running; // Value injected by FXMLLoader

    @FXML //  fx:id="threadNummerInfo"
    private TextField threadNummerInfo; // Value injected by FXMLLoader

    @FXML //  fx:id="threadPrioritaetInfo"
    private TextField threadPrioritaetInfo; // Value injected by FXMLLoader

    @FXML //  fx:id="typ"
    private TextField typ; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert dequeued != null : "fx:id=\"dequeued\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
        assert enqueued != null : "fx:id=\"enqueued\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
        assert id != null : "fx:id=\"id\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
        assert nummerbutton != null : "fx:id=\"numemrbutton\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
        assert nummerNew != null : "fx:id=\"nummerNew\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
        assert prioButton != null : "fx:id=\"prioButton\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
        assert prioNew != null : "fx:id=\"prioNew\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
        assert running != null : "fx:id=\"running\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
        assert threadNummerInfo != null : "fx:id=\"threadNummerInfo\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
        assert threadPrioritaetInfo != null : "fx:id=\"threadPrioritaetInfo\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
        assert typ != null : "fx:id=\"typ\" was not injected: check your FXML file 'ProcessorPool.fxml'.";

        WorkflowStateSummary workflowStateSummary = model.getStateSummery(engine.getId());

     
//		dequeued.setText(model.getStateSummery(engine.getId()).getNumberOfWorkflowInstancesWithState().get(WorkflowInstanceState.DEQUEUED).toString());
		enqueued.setText(Integer.toString(workflowStateSummary.getCount(WorkflowInstanceState.ENQUEUED)));
		id.setText(pool.getId());
//		running.setText(model.getStateSummery(engine.getId()).getNumberOfWorkflowInstancesWithState().get(WorkflowInstanceState.RUNNING).toString());
		typ.setText(pool.getProcessorPoolTyp().toString());
		
		threadNummerInfo.setText(String.valueOf(pool.getNumberOfThreads()));
		threadPrioritaetInfo.setText(String.valueOf(pool.getThreadPriority()));
		
		prioButton.disableProperty().bind(prioNew.textProperty().isEqualTo(""));
		prioButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (prioNew.getText()!=null && prioNew.getText().matches("\\d+")){
					dataProvider.setThreadPriority(engine.getId(), pool.getId(), Integer.valueOf(prioNew.getText()));
				}
			}
		});
		
		nummerbutton.disableProperty().bind(nummerNew.textProperty().isEqualTo(""));
		nummerbutton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (nummerNew.getText()!=null && nummerNew.getText().matches("\\d+")){
					dataProvider.setNumberOfThreads(engine.getId(), pool.getId(), Integer.valueOf(nummerNew.getText()));
				}
			}
		});
    }
	
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("ProcessorPool.fxml");
	}

}
