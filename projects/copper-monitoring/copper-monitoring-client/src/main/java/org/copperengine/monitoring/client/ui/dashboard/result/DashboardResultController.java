/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

import org.copperengine.monitoring.client.adapter.GuiCopperDataProvider;
import org.copperengine.monitoring.client.form.Form;
import org.copperengine.monitoring.client.form.filter.FilterResultControllerBase;
import org.copperengine.monitoring.client.form.filter.defaultfilter.FromToMaxCountFilterModel;
import org.copperengine.monitoring.core.model.ConfigurationInfo;
import org.copperengine.monitoring.core.model.ProcessingEngineInfo;

public class DashboardResultController extends FilterResultControllerBase<FromToMaxCountFilterModel, ConfigurationInfo> implements Initializable {
    private final GuiCopperDataProvider copperDataProvider;
    private final DashboardDependencyFactory dashboardPartsFactory;

    public DashboardResultController(GuiCopperDataProvider copperDataProvider, DashboardDependencyFactory dashboardPartsFactory) {
        super();
        this.copperDataProvider = copperDataProvider;
        this.dashboardPartsFactory = dashboardPartsFactory;
    }


    @FXML //  fx:id="leftPane"
    private VBox leftPane; // Value injected by FXMLLoader

    @FXML //  fx:id="rightPane"
    private VBox rightPane; // Value injected by FXMLLoader

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert leftPane != null : "fx:id=\"leftPane\" was not injected: check your FXML file 'DashboardResult.fxml'.";
        assert rightPane != null : "fx:id=\"rightPane\" was not injected: check your FXML file 'DashboardResult.fxml'.";

        selectedConfiguration.addListener(new ChangeListener<ConfigurationInfo>() {
            @Override
            public void changed(ObservableValue<? extends ConfigurationInfo> observableValue, ConfigurationInfo configurationInfo, ConfigurationInfo newValue) {
                if (newValue!=null){
                    update(newValue);
                }
            }
        });
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("DashboardResult.fxml");
    }

    @Override
    public void showFilteredResult(List<ConfigurationInfo> filteredlist, FromToMaxCountFilterModel usedFilter) {
        Date min=new Date(Long.MAX_VALUE);
        Date max=new Date(0);
        ConfigurationInfo maxCfgInfo = null;
        for (ConfigurationInfo configurationInfo: filteredlist){
            if (configurationInfo.getTimeStamp().before(min)){
                min=configurationInfo.getTimeStamp();
            }
            if (configurationInfo.getTimeStamp().after(max)){
                max=configurationInfo.getTimeStamp();
                maxCfgInfo = configurationInfo;
            }
        }
        minDate.set(min.getTime());
        maxDate.set(max.getTime());
        selectedConfiguration.set(maxCfgInfo);
    }

    private SimpleObjectProperty<ConfigurationInfo> selectedConfiguration = new SimpleObjectProperty<ConfigurationInfo>();
    private SimpleObjectProperty<Number> minDate = new SimpleObjectProperty<Number>(0l);
    private SimpleObjectProperty<Number> maxDate = new SimpleObjectProperty<Number>(0l);

    private final Map<String,Form<DashboardEngineController>> idToDashboardEngines = new HashMap<String,Form<DashboardEngineController>>();
    
    public void update(ConfigurationInfo configurationInfo){
        if(configurationInfo == null) return;
//        final TabPaneShowFormStrategy showFormStrategy = new TabPaneShowFormStrategy(engines);

        //remove no longer available
        Iterator<Map.Entry<String,Form<DashboardEngineController>>> iterator = idToDashboardEngines.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String,Form<DashboardEngineController>> entry = iterator.next();
            String engineId = entry.getKey();
            boolean containsId=false;
            for (ProcessingEngineInfo processingEngineInfo: configurationInfo.getEngines()) {
                if (engineId.equals(processingEngineInfo.getId())){
                    containsId=true;
                    break;
                }
            }
            if (!containsId){
                entry.getValue().close();
                iterator.remove();
            }
        }

        //add new and update existing
        int MULT = 1; // TODO - remove it after testing. Setting a value > 1 will duplicate the engine on the dashboard. This allows simulating a multi-engine Copper environment.
        for (int i=0; i<MULT * configurationInfo.getEngines().size(); i++) {
            ProcessingEngineInfo processingEngineInfo = configurationInfo.getEngines().get(i / MULT);
            String MULT_SUFFIX = "-#" + i;
            Form<DashboardEngineController> dashboardEngineForm = idToDashboardEngines.get(processingEngineInfo.getId() + MULT_SUFFIX);
            if (dashboardEngineForm==null) {
                VBox targetPane = (i % 2 == 0) ? leftPane : rightPane;
                dashboardEngineForm=dashboardPartsFactory.createDashboardEngineForm(targetPane);
                dashboardEngineForm.setAllTitle(processingEngineInfo.getId());
                idToDashboardEngines.put(processingEngineInfo.getId() + MULT_SUFFIX, dashboardEngineForm);
                dashboardEngineForm.show();
            }
            dashboardEngineForm.getController().update(processingEngineInfo);
        }
    }

    @Override
    public List<ConfigurationInfo> applyFilterInBackgroundThread(FromToMaxCountFilterModel filter) {
        return copperDataProvider.getConfigurationInfo(filter.fromToFilterModel.from.get(),filter.fromToFilterModel.to.get(),filter.maxCountFilterModel.getMaxCount());

    }

    @Override
    public void clear() {
    }
}
