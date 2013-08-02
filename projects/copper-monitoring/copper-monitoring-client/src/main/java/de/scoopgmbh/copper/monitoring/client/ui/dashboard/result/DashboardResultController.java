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
package de.scoopgmbh.copper.monitoring.client.ui.dashboard.result;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.filter.EmptyFilterModel;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultControllerBase;
import de.scoopgmbh.copper.monitoring.core.model.MonitoringDataProviderInfo;
import de.scoopgmbh.copper.monitoring.core.model.MonitoringDataStorageContentInfo;
import de.scoopgmbh.copper.monitoring.core.model.MonitoringDataStorageInfo;
import de.scoopgmbh.copper.monitoring.core.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowStateSummary;

public class DashboardResultController extends FilterResultControllerBase<EmptyFilterModel,DashboardResultModel> implements Initializable{
	private final GuiCopperDataProvider copperDataProvider;
	private final DashboardDependencyFactory dashboardPartsFactory;
	
	public DashboardResultController(GuiCopperDataProvider copperDataProvider, DashboardDependencyFactory dashboardPartsFactory) {
		super();
		this.copperDataProvider = copperDataProvider;
		this.dashboardPartsFactory = dashboardPartsFactory;
	}


    @FXML //  fx:id="countCol"
    private TableColumn<MonitoringDataStorageContentInfo, Long> countCol; // Value injected by FXMLLoader

    @FXML //  fx:id="engines"
    private TabPane engines; // Value injected by FXMLLoader

    @FXML //  fx:id="location"
    private TextField location; // Value injected by FXMLLoader

    @FXML //  fx:id="monitoringPane"
    private HBox monitoringPane; // Value injected by FXMLLoader

    @FXML //  fx:id="size"
    private TextField size; // Value injected by FXMLLoader

    @FXML //  fx:id="storageContentTable"
    private TableView<MonitoringDataStorageContentInfo> storageContentTable; // Value injected by FXMLLoader

    @FXML //  fx:id="typeCol"
    private TableColumn<MonitoringDataStorageContentInfo, String> typeCol; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert countCol != null : "fx:id=\"countCol\" was not injected: check your FXML file 'DashboardResult.fxml'.";
        assert engines != null : "fx:id=\"engines\" was not injected: check your FXML file 'DashboardResult.fxml'.";
        assert location != null : "fx:id=\"location\" was not injected: check your FXML file 'DashboardResult.fxml'.";
        assert monitoringPane != null : "fx:id=\"monitoringPane\" was not injected: check your FXML file 'DashboardResult.fxml'.";
        assert size != null : "fx:id=\"size\" was not injected: check your FXML file 'DashboardResult.fxml'.";
        assert storageContentTable != null : "fx:id=\"storageContentTable\" was not injected: check your FXML file 'DashboardResult.fxml'.";
        assert typeCol != null : "fx:id=\"typeCol\" was not injected: check your FXML file 'DashboardResult.fxml'.";



        storageContentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
	
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("DashboardResult.fxml");
	}

	@Override
	public void showFilteredResult(List<DashboardResultModel> filteredlist, EmptyFilterModel usedFilter) {
		DashboardResultModel dashboardResultModel = filteredlist.get(0);
		engines.getTabs().clear();
		for (ProcessingEngineInfo processingEngineInfo: dashboardResultModel.engines){
			dashboardPartsFactory.createEngineForm(engines,processingEngineInfo,dashboardResultModel).show();
		}
		monitoringPane.getChildren().clear();
		for (MonitoringDataProviderInfo monitoringDataProviderInfo: dashboardResultModel.providers){
			final BorderPane pane = new BorderPane();
			monitoringPane.getChildren().add(pane);
			dashboardPartsFactory.createMonitoringDataProviderForm(monitoringDataProviderInfo,pane).show();
		}
		MonitoringDataStorageInfo stoargeInfo = dashboardResultModel.monitoringDataStorageInfo;

		DecimalFormat format = new DecimalFormat("#0.000");
		double deltatInS= (stoargeInfo.getMax().getTime()-stoargeInfo.getMin().getTime())/1000;
		
		size.setText(format.format(stoargeInfo.getSizeInMb())+" mb ("+format.format(stoargeInfo.getSizeInMb()/deltatInS*1000)+" kb/s)");
		location.setText(stoargeInfo.getPath());
		storageContentTable.getItems().clear();
		storageContentTable.getItems().addAll(stoargeInfo.getMonitoringDataStorageContentInfo());
		
		typeCol.setCellValueFactory(new PropertyValueFactory<MonitoringDataStorageContentInfo,String>("type"));
		countCol.setCellValueFactory(new PropertyValueFactory<MonitoringDataStorageContentInfo,Long>("count"));
	}

	@Override
	public List<DashboardResultModel> applyFilterInBackgroundThread(EmptyFilterModel filter) {
		List<ProcessingEngineInfo> engines = copperDataProvider.getEngineList();
		Map<String, WorkflowStateSummary> engineIdTostateSummery = new HashMap<String, WorkflowStateSummary>();
		for (ProcessingEngineInfo processingEngineInfo: engines){
			engineIdTostateSummery.put(processingEngineInfo.getId(), copperDataProvider.getCopperLoadInfo(processingEngineInfo));
		}
		return Arrays.asList(new DashboardResultModel(engineIdTostateSummery,engines,
				copperDataProvider.getMonitoringDataProvider(),
				copperDataProvider.getMonitoringStorageInfo()));
	}
	
	@Override
	public void clear() {
	}
}
