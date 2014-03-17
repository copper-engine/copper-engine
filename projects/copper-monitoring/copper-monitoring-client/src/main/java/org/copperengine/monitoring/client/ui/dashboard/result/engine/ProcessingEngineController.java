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
package org.copperengine.monitoring.client.ui.dashboard.result.engine;

import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.copperengine.monitoring.client.adapter.GuiCopperDataProvider;
import org.copperengine.monitoring.client.context.FormContext;
import org.copperengine.monitoring.client.form.Form;
import org.copperengine.monitoring.client.form.FxmlController;
import org.copperengine.monitoring.client.form.dialog.DefaultInputDialogCreator;
import org.copperengine.monitoring.client.form.dialog.InputDialogCreator;
import org.copperengine.monitoring.client.ui.dashboard.result.pool.ProccessorPoolController;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo;
import org.copperengine.monitoring.core.model.ProcessorPoolInfo;

public class ProcessingEngineController implements Initializable, FxmlController {

    private final FormContext context;
    private final GuiCopperDataProvider dataProvider;
    private final InputDialogCreator inputDialogCreator;

    public ProcessingEngineController(FormContext context, GuiCopperDataProvider copperDataProvider, InputDialogCreator inputDialogCreator) {
        super();
        this.context = context;
        this.dataProvider = copperDataProvider;
        this.inputDialogCreator = inputDialogCreator;
    }

    @FXML
    // fx:id="batcherId"
    private TextField batcherId; // Value injected by FXMLLoader

    @FXML
    // fx:id="batcherNumSet"
    private Button batcherNumSet; // Value injected by FXMLLoader

    @FXML
    // fx:id="batcherthreadnum"
    private TextField batcherthreadnum; // Value injected by FXMLLoader

    @FXML
    // fx:id="id"
    private TextField id; // Value injected by FXMLLoader

    @FXML
    // fx:id="injectorTyp"
    private TextField injectorTyp; // Value injected by FXMLLoader

    @FXML
    // fx:id="statisticsCollector"
    private TextField statisticsCollector; // Value injected by FXMLLoader

    @FXML
    // fx:id="pools"
    private TabPane pools; // Value injected by FXMLLoader

    @FXML
    // fx:id="state_ENQUEUED"
    private TextField state_ENQUEUED; // Value injected by FXMLLoader

    @FXML
    // fx:id="state_ERROR"
    private TextField state_ERROR; // Value injected by FXMLLoader

    @FXML
    // fx:id="state_FINISHED"
    private TextField state_FINISHED; // Value injected by FXMLLoader

    @FXML
    // fx:id="state_INVALID"
    private TextField state_INVALID; // Value injected by FXMLLoader

    @FXML
    // fx:id="state_WAITING"
    private TextField state_WAITING; // Value injected by FXMLLoader

    @FXML
    // fx:id="storageId"
    private TextField storageId; // Value injected by FXMLLoader

    @FXML
    // fx:id="typ"
    private TextField typ; // Value injected by FXMLLoader

    @FXML
    // fx:id="workflowRepositoryId"
    private TextField workflowRepositoryId; // Value injected by FXMLLoader

    @FXML
    // fx:id="workflowRepositoryPaths"
    private TextArea workflowRepositoryPaths; // Value injected by FXMLLoader

    @FXML
    // fx:id="workflowRepositoryTyp"
    private TextField workflowRepositoryTyp; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert batcherId != null : "fx:id=\"batcherId\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert batcherNumSet != null : "fx:id=\"batcherNumSet\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert batcherthreadnum != null : "fx:id=\"batcherthreadnum\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert id != null : "fx:id=\"id\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert injectorTyp != null : "fx:id=\"injectorTyp\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert pools != null : "fx:id=\"pools\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert state_ENQUEUED != null : "fx:id=\"state_ENQUEUED\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert state_ERROR != null : "fx:id=\"state_ERROR\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert state_FINISHED != null : "fx:id=\"state_FINISHED\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert state_INVALID != null : "fx:id=\"state_INVALID\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert state_WAITING != null : "fx:id=\"state_WAITING\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert storageId != null : "fx:id=\"storageId\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert typ != null : "fx:id=\"typ\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert workflowRepositoryId != null : "fx:id=\"workflowRepositoryId\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert workflowRepositoryPaths != null : "fx:id=\"workflowRepositoryPaths\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";
        assert workflowRepositoryTyp != null : "fx:id=\"workflowRepositoryTyp\" was not injected: check your FXML file 'ProcessingEngine.fxml'.";


        pools.getStyleClass().add("floating");// transparent tabheader

        batcherNumSet.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                inputDialogCreator.showIntInputDialog("Number of threads", Integer.valueOf(batcherthreadnum.getText()), new DefaultInputDialogCreator.DialogClosed<Integer>() {
                    @Override
                    public void closed(Integer inputValue) {
                        dataProvider.setBatcherNumThreads(id.getText(), inputValue);
                        context.createDashboardForm().refresh();
                    }
                });
            }
        });
        batcherNumSet.disableProperty().bind(batcherId.textProperty().isEqualTo(""));
    }

    private final Map<String, ProccessorPoolController> poolControllers = new TreeMap<String, ProccessorPoolController>();

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("ProcessingEngine.fxml");
    }

    public void update(ProcessingEngineInfo processingEngineInfo){

        id.setText(processingEngineInfo.getId());
        // row.setText(dashboardResultModel.getStateSummery(processingEngineInfo.getId()).getNumberOfWorkflowInstancesWithState().get(WorkflowInstanceState.RAW).toString());
        typ.setText(processingEngineInfo.getTyp().toString());
        workflowRepositoryId.setText(processingEngineInfo.getRepositoryInfo().getName());
        workflowRepositoryTyp.setText(processingEngineInfo.getRepositoryInfo().getWorkflowRepositorTyp().toString());
        workflowRepositoryPaths.setText("");
        for (String path : processingEngineInfo.getRepositoryInfo().getSrcPaths()) {
            workflowRepositoryPaths.setText(workflowRepositoryPaths.getText() + path + "\n");
        }
        injectorTyp.setText(processingEngineInfo.getDependencyInjectorInfo().getTyp());
        statisticsCollector.setText(processingEngineInfo.getStatisticsCollectorType());
        storageId.setText(processingEngineInfo.getStorageInfo().getDescription());
        batcherId.setText(processingEngineInfo.getStorageInfo().getBatcher().getDescription());
        batcherthreadnum.setText(Integer.toString(processingEngineInfo.getStorageInfo().getBatcher().getNumThreads()));

        state_ENQUEUED.setText("?");//Integer.toString(workflowStateSummary.getCount(WorkflowInstanceState.ENQUEUED)));
        state_ERROR.setText("?");//Integer.toString(workflowStateSummary.getCount(WorkflowInstanceState.ERROR)));
        state_FINISHED.setText("?");//Integer.toString(workflowStateSummary.getCount(WorkflowInstanceState.FINISHED)));
        state_INVALID.setText("?");//Integer.toString(workflowStateSummary.getCount(WorkflowInstanceState.INVALID)));
        state_WAITING.setText("?");//Integer.toString(workflowStateSummary.getCount(WorkflowInstanceState.WAITING)));



        //dashboardResultModel.getStateSummery(processingEngineInfo.getId()

        updateProcessorPools(processingEngineInfo);
    }

    private void updateProcessorPools(ProcessingEngineInfo processingEngineInfo) {
        Set<String> poolIds = new HashSet<String>();
        for (ProcessorPoolInfo processorPoolInfo : processingEngineInfo.getPools()) {
            poolIds.add(processorPoolInfo.getId());
        }
        boolean poolsChanged = !poolIds.equals(poolControllers.keySet());
        if (poolsChanged) {
            pools.getTabs().clear();
            poolControllers.clear();
            for (ProcessorPoolInfo processorPoolInfo : processingEngineInfo.getPools()) {
                Form<ProccessorPoolController> poolForm = context.createPoolForm(pools, processingEngineInfo, processorPoolInfo);
                String id = processorPoolInfo.getId();
                poolControllers.put(id, poolForm.getController());
                poolForm.show();
            }
        } else {
            for (ProcessorPoolInfo processorPoolInfo : processingEngineInfo.getPools()) {
                String id = processorPoolInfo.getId();
                ProccessorPoolController poolController = poolControllers.get(id);
                poolController.setPool(processorPoolInfo);
            }
        }
    }

}
