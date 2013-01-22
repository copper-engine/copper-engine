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
package de.scoopgmbh.copper.gui.ui.dashboard.result.engine;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import de.scoopgmbh.copper.gui.context.FormContext;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.ui.dashboard.result.DashboardResultModel;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessorPoolInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;

public class ProcessingEngineController implements Initializable, FxmlController {
	private final ProcessingEngineInfo processingEngineInfo;
	private final DashboardResultModel dashboardResultModel;
	private final FormContext context;
	public ProcessingEngineController(ProcessingEngineInfo processingEngineInfo, DashboardResultModel dashboardResultModel, FormContext context) {
		super();
		this.processingEngineInfo = processingEngineInfo;
		this.dashboardResultModel = dashboardResultModel;
		this.context = context;
	}


    @FXML //  fx:id="finished"
    private TextField finished; // Value injected by FXMLLoader

    @FXML //  fx:id="id"
    private TextField id; // Value injected by FXMLLoader

    @FXML //  fx:id="pools"
    private TabPane pools; // Value injected by FXMLLoader

    @FXML //  fx:id="row"
    private TextField row; // Value injected by FXMLLoader

    @FXML //  fx:id="typ"
    private TextField typ; // Value injected by FXMLLoader

    @FXML //  fx:id="waiting"
    private TextField waiting; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert finished != null : "fx:id=\"finished\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert id != null : "fx:id=\"id\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert pools != null : "fx:id=\"pools\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert row != null : "fx:id=\"row\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert typ != null : "fx:id=\"typ\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert waiting != null : "fx:id=\"waiting\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";

        finished.setText(dashboardResultModel.getStateSummery(processingEngineInfo.getId()).getNumberOfWorkflowInstancesWithState().get(WorkflowInstanceState.FINISHED).toString());
        id.setText(processingEngineInfo.getId());
        row.setText(dashboardResultModel.getStateSummery(processingEngineInfo.getId()).getNumberOfWorkflowInstancesWithState().get(WorkflowInstanceState.RAW).toString());
        typ.setText(processingEngineInfo.getTyp().toString());
        waiting.setText(dashboardResultModel.getStateSummery(processingEngineInfo.getId()).getNumberOfWorkflowInstancesWithState().get(WorkflowInstanceState.WAITING).toString());
        
        pools.getStyleClass().add("floating");//transparent tabheader
        for (ProcessorPoolInfo processorPoolInfo: processingEngineInfo.getPools()){
        	context.createPoolForm(pools,processingEngineInfo,processorPoolInfo,dashboardResultModel).show();
        }
    }

	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("ProcessingEngine.fxml");
	}

}
