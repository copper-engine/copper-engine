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
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import org.copperengine.monitoring.client.adapter.GuiCopperDataProvider;
import org.copperengine.monitoring.client.form.Form;
import org.copperengine.monitoring.client.form.filter.FilterResultControllerBase;
import org.copperengine.monitoring.client.form.filter.defaultfilter.FromToMaxCountFilterModel;
import org.copperengine.monitoring.client.ui.dashboard.result.engines.ProcessingEnginesController;
import org.copperengine.monitoring.client.ui.dashboard.result.provider.ProviderController;
import org.copperengine.monitoring.client.util.DateTimePicker;
import org.copperengine.monitoring.client.util.DelayedUIExecutor;
import org.copperengine.monitoring.client.util.TableColumnHelper;
import org.copperengine.monitoring.core.model.ConfigurationInfo;
import org.copperengine.monitoring.core.model.MonitoringDataProviderInfo;
import org.copperengine.monitoring.core.model.MonitoringDataStorageContentInfo;
import org.copperengine.monitoring.core.model.MonitoringDataStorageDetailInfo;
import org.copperengine.monitoring.core.model.MonitoringDataStorageInfo;

public class DashboardResultController extends FilterResultControllerBase<FromToMaxCountFilterModel, ConfigurationInfo> implements Initializable {
    private final GuiCopperDataProvider copperDataProvider;
    private final DashboardDependencyFactory dashboardPartsFactory;
    private Form<ProcessingEnginesController> enginesForm;

    private final ObservableList<Integer> storageDetailMinutesOptions = 
            FXCollections.observableArrayList(5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 
                    90, 120, 180, 240, 300, 360, 420, 480, 540, 600, 660, 720, 1080, 1440, 2880);

    
    public DashboardResultController(GuiCopperDataProvider copperDataProvider, DashboardDependencyFactory dashboardPartsFactory) {
        super();
        this.copperDataProvider = copperDataProvider;
        this.dashboardPartsFactory = dashboardPartsFactory;
    }


    @FXML //  fx:id="countCol"
    private TableColumn<MonitoringDataStorageContentInfo, Long> countCol; // Value injected by FXMLLoader

    @FXML //  fx:id="enginesTarget"
    private Pane enginesTarget; // Value injected by FXMLLoader

    @FXML //  fx:id="location"
    private TextField location; // Value injected by FXMLLoader

    @FXML //  fx:id="monitoringPane"
    private VBox monitoringPane; // Value injected by FXMLLoader

    @FXML //  fx:id="size"
    private TextField size; // Value injected by FXMLLoader

    @FXML //  fx:id="storageContentTable"
    private TableView<MonitoringDataStorageContentInfo> storageContentTable; // Value injected by FXMLLoader

    @FXML //  fx:id="typeCol"
    private TableColumn<MonitoringDataStorageContentInfo, String> typeCol; // Value injected by FXMLLoader

    @FXML
    private Slider timeSlider;

    @FXML
    private Pane datePickerTarget;

    @FXML
    private ComboBox<Integer> storageDetailMinutes;

    @FXML
    private Pane disableOverlay;


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert countCol != null : "fx:id=\"countCol\" was not injected: check your FXML file 'DashboardResult.fxml'.";
        assert enginesTarget != null : "fx:id=\"enginesTarget\" was not injected: check your FXML file 'DashboardResult.fxml'.";
        assert location != null : "fx:id=\"location\" was not injected: check your FXML file 'DashboardResult.fxml'.";
        assert monitoringPane != null : "fx:id=\"monitoringPane\" was not injected: check your FXML file 'DashboardResult.fxml'.";
        assert size != null : "fx:id=\"size\" was not injected: check your FXML file 'DashboardResult.fxml'.";
        assert storageContentTable != null : "fx:id=\"storageContentTable\" was not injected: check your FXML file 'DashboardResult.fxml'.";
        assert typeCol != null : "fx:id=\"typeCol\" was not injected: check your FXML file 'DashboardResult.fxml'.";
        assert storageDetailMinutes != null : "fx:id=\"storageDetailMinutes\" was not injected: check your FXML file 'DashboardResult.fxml'.";
        
        enginesForm = dashboardPartsFactory.createEnginesForm(enginesTarget);
        enginesForm.show();


        timeSlider.minProperty().bindBidirectional(minDate);
        timeSlider.maxProperty().bindBidirectional(maxDate);
        timeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number newValue) {
                if (newValue!=null){
                    final Date date = new Date(newValue.longValue());
                    setFromSlider=true;
                    selectedConfiguration.set(findByDate(lastResults, date));
                    setFromSlider=false;
                }
            }
        });
        final DateTimePicker dateTimePicker = new DateTimePicker();
        selectedConfiguration.addListener(new ChangeListener<ConfigurationInfo>() {
            @Override
            public void changed(ObservableValue<? extends ConfigurationInfo> observableValue, ConfigurationInfo configurationInfo, ConfigurationInfo newValue) {
                if (newValue!=null){
                    dateTimePicker.selectedDateProperty().set(newValue.getTimeStamp());
                    if (!setFromSlider){
                        timeSlider.setValue(newValue.getTimeStamp().getTime());
                    }
                    updateEngines(newValue);
                    showMonitoringData(newValue);
                    showDataStorage(newValue);
                    disableOverlay.setVisible(!lastResults.isEmpty() && newValue!=lastResults.get(0));
                }
            }
        });
        datePickerTarget.getChildren().add(dateTimePicker.createContent());

        storageDetailMinutes.setItems(storageDetailMinutesOptions);
        
        storageDetailMinutes.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                updateStorageDetails();
            }
        });
        
        TableColumnHelper.setTextOverrunCellFactory(typeCol, OverrunStyle.LEADING_ELLIPSIS);
        typeCol.prefWidthProperty().bind(storageContentTable.widthProperty().subtract(2).multiply(0.75));
        countCol.prefWidthProperty().bind(storageContentTable.widthProperty().subtract(2).multiply(0.25));

        storageDetailMinutes.setValue(storageDetailMinutesOptions.get(0));
        updateStorageDetails();
    }

    private void updateStorageDetails() {
        storageContentTable.getItems().clear();
        Date fromDate = null;
        Integer minutes = storageDetailMinutes.getValue();
        if(minutes != null) {
            fromDate = new Date(System.currentTimeMillis() - 60000L * minutes);
        }
        MonitoringDataStorageDetailInfo monitoringDataStorageDetailInfo = copperDataProvider.getMonitoringDataStorageDetailInfo(fromDate, null);
        storageContentTable.getItems().addAll(monitoringDataStorageDetailInfo.getMonitoringDataStorageContentInfo());
    }
    
    boolean setFromSlider=false;

    public ConfigurationInfo findByDate(List<ConfigurationInfo> result, Date date){
        ConfigurationInfo previousConfigurationInfo=null;
        for (ConfigurationInfo configurationInfo: result){
            if (previousConfigurationInfo==null){
                if (date.getTime()>=configurationInfo.getTimeStamp().getTime()){
                    return configurationInfo;
                }
            } else {
                if (date.getTime() <= previousConfigurationInfo.getTimeStamp().getTime() && date.getTime() >= configurationInfo.getTimeStamp().getTime()){
                    return configurationInfo;
                }
            }
            previousConfigurationInfo=configurationInfo;
        }
        return null;
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("DashboardResult.fxml");
    }

    @Override
    public void showFilteredResult(List<ConfigurationInfo> filteredlist, FromToMaxCountFilterModel usedFilter) {
        lastResults=filteredlist;
        Date min=new Date(Long.MAX_VALUE);
        Date max=new Date(0);
        for (ConfigurationInfo configurationInfo: filteredlist){
            if (configurationInfo.getTimeStamp().before(min)){
                min=configurationInfo.getTimeStamp();
            }
            if (configurationInfo.getTimeStamp().after(max)){
                max=configurationInfo.getTimeStamp();
            }
        }
        minDate.set(min.getTime());
        maxDate.set(max.getTime());
        if (!filteredlist.isEmpty()){
            selectedConfiguration.set(filteredlist.get(0));
        }
    }

    private SimpleObjectProperty<ConfigurationInfo> selectedConfiguration = new SimpleObjectProperty<ConfigurationInfo>();
    private SimpleObjectProperty<Number> minDate = new SimpleObjectProperty<Number>(0l);
    private SimpleObjectProperty<Number> maxDate = new SimpleObjectProperty<Number>(0l);
    private List<ConfigurationInfo> lastResults;

    private void updateEngines(ConfigurationInfo configurationInfo) {
        enginesForm.getController().update(configurationInfo);
    }

    private void showDataStorage(ConfigurationInfo configurationInfo) {
        MonitoringDataStorageInfo storageInfo = configurationInfo.getMonitoringDataStorageInfo();
        DecimalFormat format = new DecimalFormat("#0.000");
        double deltatInS = (storageInfo.getMax().getTime() - storageInfo.getMin().getTime()) / 1000;

        size.setText(format.format(storageInfo.getSizeInMb()) + " mb (" + format.format(storageInfo.getSizeInMb() / deltatInS * 1000) + " kb/s)");
        location.setText(storageInfo.getPath());

        typeCol.setCellValueFactory(new PropertyValueFactory<MonitoringDataStorageContentInfo, String>("type"));
        countCol.setCellValueFactory(new PropertyValueFactory<MonitoringDataStorageContentInfo, Long>("count"));
    }

    private final Map<String, ProviderController> monitoringDataProviders = new TreeMap<String, ProviderController>();

    private void showMonitoringData(ConfigurationInfo configurationInfo) {
        Set<String> monitoringDataNames = new HashSet<String>();
        for (MonitoringDataProviderInfo monitoringDataProviderInfo : configurationInfo.getProviders()) {
            monitoringDataNames.add(monitoringDataProviderInfo.getName());
        }
        boolean monitoringDataChanged = !monitoringDataNames.equals(monitoringDataProviders.keySet());
        if (monitoringDataChanged) {
            monitoringDataProviders.clear();
            monitoringPane.getChildren().clear();
            for (MonitoringDataProviderInfo monitoringDataProviderInfo : configurationInfo.getProviders()) {
                final BorderPane pane = new BorderPane();
                monitoringPane.getChildren().add(pane);
                Form<ProviderController> providerForm = dashboardPartsFactory.createMonitoringDataProviderForm(monitoringDataProviderInfo, pane);
                String name = monitoringDataProviderInfo.getName();
                monitoringDataProviders.put(name, providerForm.getController());
                providerForm.show();
            }
        } else {
            for (int i = 0; i < configurationInfo.getProviders().size(); i++) {
                MonitoringDataProviderInfo providerInfo = configurationInfo.getProviders().get(i);
                String name = providerInfo.getName();
                ProviderController controller = monitoringDataProviders.get(name);
                String status = providerInfo.getStatus();
                controller.getStatus().setText(status);
            }
        }
    }

    @Override
    public List<ConfigurationInfo> applyFilterInBackgroundThread(FromToMaxCountFilterModel filter) {
        int maxCount = filter.maxCountFilterModel.getMaxCount();
        List<ConfigurationInfo> configurationInfo = copperDataProvider.getConfigurationInfo(filter.fromToFilterModel.from.get(),filter.fromToFilterModel.to.get(),maxCount);
        DelayedUIExecutor executor = new DelayedUIExecutor() {            
            @Override public void execute() { updateStorageDetails(); }
        };
        executor.executeWithDelays(0, 1, 3, 10);
        return configurationInfo;

    }

    @Override
    public void clear() {
    }
}
