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
package org.copperengine.monitoring.client.ui.dashboard.result;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import org.copperengine.monitoring.client.adapter.GuiCopperDataProvider;
import org.copperengine.monitoring.client.form.Form;
import org.copperengine.monitoring.client.form.filter.EmptyFilterModel;
import org.copperengine.monitoring.client.form.filter.FilterResultControllerBase;
import org.copperengine.monitoring.client.ui.dashboard.result.engine.ProcessingEngineController;
import org.copperengine.monitoring.client.ui.dashboard.result.provider.ProviderController;
import org.copperengine.monitoring.core.model.MonitoringDataProviderInfo;
import org.copperengine.monitoring.core.model.MonitoringDataStorageContentInfo;
import org.copperengine.monitoring.core.model.MonitoringDataStorageInfo;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo;
import org.copperengine.monitoring.core.model.WorkflowStateSummary;

public class DashboardResultController extends FilterResultControllerBase<EmptyFilterModel, DashboardResultModel> implements Initializable {
    private final GuiCopperDataProvider copperDataProvider;
    private final DashboardDependencyFactory dashboardPartsFactory;

    public DashboardResultController(GuiCopperDataProvider copperDataProvider, DashboardDependencyFactory dashboardPartsFactory) {
        super();
        this.copperDataProvider = copperDataProvider;
        this.dashboardPartsFactory = dashboardPartsFactory;
    }

    @FXML
    // fx:id="countCol"
    private TableColumn<MonitoringDataStorageContentInfo, Long> countCol; // Value injected by FXMLLoader

    @FXML
    // fx:id="engines"
    private TabPane engines; // Value injected by FXMLLoader

    @FXML
    // fx:id="location"
    private TextField location; // Value injected by FXMLLoader

    @FXML
    // fx:id="monitoringPane"
    private HBox monitoringPane; // Value injected by FXMLLoader

    @FXML
    // fx:id="size"
    private TextField size; // Value injected by FXMLLoader

    @FXML
    // fx:id="storageContentTable"
    private TableView<MonitoringDataStorageContentInfo> storageContentTable; // Value injected by FXMLLoader

    @FXML
    // fx:id="typeCol"
    private TableColumn<MonitoringDataStorageContentInfo, String> typeCol; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
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
    public URL getFxmlResource() {
        return getClass().getResource("DashboardResult.fxml");
    }

    @Override
    public void showFilteredResult(List<DashboardResultModel> filteredlist, EmptyFilterModel usedFilter) {
        DashboardResultModel dashboardResultModel = filteredlist.get(0);
        showEngines(dashboardResultModel);
        showMonitoringData(dashboardResultModel);
        showDataStorage(dashboardResultModel);
    }

    private void showDataStorage(DashboardResultModel dashboardResultModel) {
        MonitoringDataStorageInfo storageInfo = dashboardResultModel.monitoringDataStorageInfo;
        boolean storageChanged = true;
        if (storageChanged) {
            DecimalFormat format = new DecimalFormat("#0.000");
            double deltatInS = (storageInfo.getMax().getTime() - storageInfo.getMin().getTime()) / 1000;

            size.setText(format.format(storageInfo.getSizeInMb()) + " mb (" + format.format(storageInfo.getSizeInMb() / deltatInS * 1000) + " kb/s)");
            location.setText(storageInfo.getPath());
            storageContentTable.getItems().clear();
            storageContentTable.getItems().addAll(storageInfo.getMonitoringDataStorageContentInfo());

            typeCol.setCellValueFactory(new PropertyValueFactory<MonitoringDataStorageContentInfo, String>("type"));
            countCol.setCellValueFactory(new PropertyValueFactory<MonitoringDataStorageContentInfo, Long>("count"));
        }
    }

    private final Map<String, ProcessingEngineController> engineControllers = new TreeMap<String, ProcessingEngineController>();

    private void showEngines(DashboardResultModel dashboardResultModel) {
        Set<String> engineIds = new HashSet<String>();
        for (ProcessingEngineInfo processingEngineInfo : dashboardResultModel.engines) {
            engineIds.add(processingEngineInfo.getId());
        }
        boolean enginesChanged = !engineIds.equals(engineControllers.keySet());
        if (enginesChanged) {
            engineControllers.clear();
            engines.getTabs().clear();
            for (ProcessingEngineInfo processingEngineInfo : dashboardResultModel.engines) {
                Form<ProcessingEngineController> engineForm = dashboardPartsFactory.createEngineForm(engines, processingEngineInfo, dashboardResultModel);
                String id = processingEngineInfo.getId();
                engineControllers.put(id, engineForm.getController());
                engineForm.show();
            }
        } else {
            for (ProcessingEngineInfo processingEngineInfo : dashboardResultModel.engines) {
                String id = processingEngineInfo.getId();
                ProcessingEngineController controller = engineControllers.get(id);
                controller.setProcessingEngineInfo(processingEngineInfo);
            }
        }
    }

    private final Map<String, ProviderController> monitoringDataProviders = new TreeMap<String, ProviderController>();

    private void showMonitoringData(DashboardResultModel dashboardResultModel) {
        Set<String> monitoringDataNames = new HashSet<String>();
        for (MonitoringDataProviderInfo monitoringDataProviderInfo : dashboardResultModel.providers) {
            monitoringDataNames.add(monitoringDataProviderInfo.getName());
        }
        boolean monitoringDataChanged = !monitoringDataNames.equals(monitoringDataProviders.keySet());
        if (monitoringDataChanged) {
            System.err.println("monitoringDataChanged. New names: " + monitoringDataNames);
            monitoringDataProviders.clear();
            monitoringPane.getChildren().clear();
            for (MonitoringDataProviderInfo monitoringDataProviderInfo : dashboardResultModel.providers) {
                final BorderPane pane = new BorderPane();
                monitoringPane.getChildren().add(pane);
                Form<ProviderController> providerForm = dashboardPartsFactory.createMonitoringDataProviderForm(monitoringDataProviderInfo, pane);
                String name = monitoringDataProviderInfo.getName();
                monitoringDataProviders.put(name, providerForm.getController());
                providerForm.show();
            }
        } else {
            for (int i = 0; i < dashboardResultModel.providers.size(); i++) {
                MonitoringDataProviderInfo providerInfo = dashboardResultModel.providers.get(i);
                String name = providerInfo.getName();
                ProviderController controller = monitoringDataProviders.get(name);
                String status = providerInfo.getStatus();
                controller.getStatus().setText(status);
            }
        }
    }

    @Override
    public List<DashboardResultModel> applyFilterInBackgroundThread(EmptyFilterModel filter) {
        List<ProcessingEngineInfo> engines = copperDataProvider.getEngineList();
        Map<String, WorkflowStateSummary> engineIdToStateSummery = new HashMap<String, WorkflowStateSummary>();
        for (ProcessingEngineInfo processingEngineInfo : engines) {
            engineIdToStateSummery.put(processingEngineInfo.getId(), copperDataProvider.getCopperLoadInfo(processingEngineInfo));
        }
        return Arrays.asList(new DashboardResultModel(engineIdToStateSummery, engines,
                copperDataProvider.getMonitoringDataProvider(),
                copperDataProvider.getMonitoringStorageInfo()));
    }

    @Override
    public void clear() {
    }
}
