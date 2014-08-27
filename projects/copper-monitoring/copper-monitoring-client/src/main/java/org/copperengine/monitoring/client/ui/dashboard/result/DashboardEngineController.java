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
package org.copperengine.monitoring.client.ui.dashboard.result;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;

import org.copperengine.monitoring.client.adapter.GuiCopperDataProvider;
import org.copperengine.monitoring.client.form.FxmlController;
import org.copperengine.monitoring.client.ui.workflowsummary.filter.WorkflowSummaryFilterModel;
import org.copperengine.monitoring.client.ui.workflowsummary.result.WorkflowSummaryResultModel;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo;
import org.copperengine.monitoring.core.model.ProcessorPoolInfo;
import org.copperengine.monitoring.core.model.ProcessorPoolInfo.ProcessorPoolTyp;
import org.copperengine.monitoring.core.model.WorkflowInstanceState;

public class DashboardEngineController implements Initializable, FxmlController {

    private final GuiCopperDataProvider dataProvider;

    public DashboardEngineController(GuiCopperDataProvider copperDataProvider) {
        this.dataProvider = copperDataProvider;
    }

    @FXML
    // fx:id="engineId"
    private Label engineId; // Value injected by FXMLLoader

    @FXML
    // fx:id="imgEngineState"
    private ImageView imgEngineState; // Value injected by FXMLLoader

    @FXML
    // fx:id="tableWfInstances"
    private TableView<WorkflowSummaryDashboardModel> tableWfInstances; // Value injected by FXMLLoader

    @FXML
    // fx:id="colWfState"
    private TableColumn<WorkflowSummaryDashboardModel, WorkflowInstanceState> colWfState; // Value injected by FXMLLoader

    @FXML
    // fx:id="colWfCount"
    private TableColumn<WorkflowSummaryDashboardModel, Integer> colWfCount; // Value injected by FXMLLoader

    
    @FXML
    // fx:id="tableProcessorPools"
    private TableView<ProcessorPoolDashboardModel> tableProcessorPools; // Value injected by FXMLLoader

    @FXML
    // fx:id="colPoolName"
    private TableColumn<ProcessorPoolDashboardModel, String> colPoolName; // Value injected by FXMLLoader
    
    @FXML
    // fx:id="colQueueSize"
    private TableColumn<ProcessorPoolDashboardModel, Integer> colQueueSize; // Value injected by FXMLLoader
    
    @FXML
    // fx:id="colThreadCount"
    private TableColumn<ProcessorPoolDashboardModel, Integer> colThreadCount; // Value injected by FXMLLoader
    
    @FXML
    // fx:id="colPoolType"
    private TableColumn<ProcessorPoolDashboardModel, ProcessorPoolTyp> colPoolType; // Value injected by FXMLLoader
    
    
    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert engineId != null : "fx:id=\"engineId\" was not injected: check your FXML file 'DashboardEngine.fxml'.";
        assert imgEngineState != null : "fx:id=\"imgEngineState\" was not injected: check your FXML file 'DashboardEngine.fxml'.";
        assert tableWfInstances != null : "fx:id=\"tableWfInstances\" was not injected: check your FXML file 'DashboardEngine.fxml'.";
        assert colWfState != null : "fx:id=\"colWfState\" was not injected: check your FXML file 'DashboardEngine.fxml'.";
        assert colWfCount != null : "fx:id=\"colWfCount\" was not injected: check your FXML file 'DashboardEngine.fxml'.";
        assert tableProcessorPools != null : "fx:id=\"tableProcessorPools\" was not injected: check your FXML file 'DashboardEngine.fxml'.";
        assert colPoolName != null : "fx:id=\"colPoolName\" was not injected: check your FXML file 'DashboardEngine.fxml'.";
        assert colQueueSize != null : "fx:id=\"colQueueSize\" was not injected: check your FXML file 'DashboardEngine.fxml'.";
        assert colThreadCount != null : "fx:id=\"colThreadCount\" was not injected: check your FXML file 'DashboardEngine.fxml'.";
        assert colPoolType != null : "fx:id=\"colPoolType\" was not injected: check your FXML file 'DashboardEngine.fxml'.";

        colWfState.setCellValueFactory(new PropertyValueFactory<WorkflowSummaryDashboardModel, WorkflowInstanceState>("state"));
        colWfCount.setCellValueFactory(new PropertyValueFactory<WorkflowSummaryDashboardModel, Integer>("count"));
        
        colPoolName.setCellValueFactory(new PropertyValueFactory<ProcessorPoolDashboardModel, String>("poolName"));
        colQueueSize.setCellValueFactory(new PropertyValueFactory<ProcessorPoolDashboardModel, Integer>("queueSize"));
        colThreadCount.setCellValueFactory(new PropertyValueFactory<ProcessorPoolDashboardModel, Integer>("threadCount"));
        colPoolType.setCellValueFactory(new PropertyValueFactory<ProcessorPoolDashboardModel, ProcessorPoolTyp>("poolType"));
        
        imgEngineState.getStyleClass().add("copper-engine-logo");        
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("DashboardEngine.fxml");
    }

    public void update(ProcessingEngineInfo processingEngineInfo){
        
        engineId.setText(processingEngineInfo.getId());
        
        WorkflowSummaryFilterModel wfFilter = new WorkflowSummaryFilterModel();
        List<WorkflowSummaryResultModel> workflowSummary = dataProvider.getWorkflowSummary(wfFilter);
        ObservableList<WorkflowSummaryDashboardModel> wfDashboardModel = FXCollections.observableArrayList();        
        for(WorkflowSummaryResultModel wfModel : workflowSummary) {
            for(WorkflowInstanceState state : WorkflowInstanceState.values()) {
                int count = wfModel.workflowStateSummary.getCount(state);
                wfDashboardModel.add(new WorkflowSummaryDashboardModel(state, count));
            }
        }
        tableWfInstances.setItems(wfDashboardModel);

        
        List<ProcessorPoolInfo> pools = processingEngineInfo.getPools();
        ObservableList<ProcessorPoolDashboardModel> poolDashboardModel = FXCollections.observableArrayList();        
        for(ProcessorPoolInfo poolInfo : pools) {
            String poolName = poolInfo.getId();
//            int queueSize = poolInfo.getDequeueBulkSize();
            int queueSize = poolInfo.getMemoryQueueSize();
            int threadCount = poolInfo.getNumberOfThreads();
            ProcessorPoolTyp poolType = poolInfo.getProcessorPoolTyp();
            poolDashboardModel.add(new ProcessorPoolDashboardModel(poolName, queueSize, threadCount, poolType));
        }
        tableProcessorPools.setItems(poolDashboardModel);
    }
}
