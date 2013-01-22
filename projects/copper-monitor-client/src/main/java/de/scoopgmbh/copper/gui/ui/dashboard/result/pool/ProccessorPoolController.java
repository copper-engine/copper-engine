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

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.ui.dashboard.result.DashboardResultModel;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessorPoolInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;

public class ProccessorPoolController implements Initializable, FxmlController {
	private final ProcessorPoolInfo pool;
	private final ProcessingEngineInfo engine;
	private final DashboardResultModel model;

    public ProccessorPoolController(ProcessingEngineInfo engine, ProcessorPoolInfo pool, DashboardResultModel model) {
    	this.pool=pool;
    	this.model=model;
    	this.engine = engine;
	}


    @FXML //  fx:id="dequeued"
    private TextField dequeued; // Value injected by FXMLLoader

    @FXML //  fx:id="enqueued"
    private TextField enqueued; // Value injected by FXMLLoader

    @FXML //  fx:id="id"
    private TextField id; // Value injected by FXMLLoader

    @FXML //  fx:id="running"
    private TextField running; // Value injected by FXMLLoader

    @FXML //  fx:id="typ"
    private TextField typ; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert dequeued != null : "fx:id=\"dequeued\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
        assert enqueued != null : "fx:id=\"enqueued\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
        assert id != null : "fx:id=\"id\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
        assert running != null : "fx:id=\"running\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
        assert typ != null : "fx:id=\"typ\" was not injected: check your FXML file 'ProcessorPool.fxml'.";
     
		dequeued.setText(model.getStateSummery(engine.getId()).getNumberOfWorkflowInstancesWithState().get(WorkflowInstanceState.DEQUEUED).toString());
		enqueued.setText(model.getStateSummery(engine.getId()).getNumberOfWorkflowInstancesWithState().get(WorkflowInstanceState.ENQUEUED).toString());
		id.setText(pool.getId());
		running.setText(model.getStateSummery(engine.getId()).getNumberOfWorkflowInstancesWithState().get(WorkflowInstanceState.RUNNING).toString());
		typ.setText(pool.getProcessorPoolTyp().toString());
    }
	
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("ProcessorPool.fxml");
	}

}
