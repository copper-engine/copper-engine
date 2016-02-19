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
package org.copperengine.monitoring.client.ui.load.result;

import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;

import org.copperengine.monitoring.client.adapter.GuiCopperDataProvider;
import org.copperengine.monitoring.client.form.filter.FilterResultControllerBase;
import org.copperengine.monitoring.client.ui.load.filter.EngineLoadFilterModel;
import org.copperengine.monitoring.client.util.ComponentUtil;
import org.copperengine.monitoring.core.model.WorkflowInstanceState;
import org.copperengine.monitoring.core.model.WorkflowStateSummary;

public class EngineLoadResultController extends FilterResultControllerBase<EngineLoadFilterModel, WorkflowStateSummary> implements Initializable {
    private final GuiCopperDataProvider copperDataProvider;

    public EngineLoadResultController(GuiCopperDataProvider copperDataProvider) {
        super();
        this.copperDataProvider = copperDataProvider;
    }

    @FXML
    // fx:id="areaChart"
    private AreaChart<Number, Number> areaChart; // Value injected by FXMLLoader

    @FXML
    // fx:id="yAxis"
    private NumberAxis xAxis; // Value injected by FXMLLoader

    @FXML
    // fx:id="numberAxis"
    private NumberAxis numberAxis; // Value injected by FXMLLoader

    private final Map<WorkflowInstanceState, XYChart.Series<Number, Number>> stateToAxis = new HashMap<WorkflowInstanceState, XYChart.Series<Number, Number>>();

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert areaChart != null : "fx:id=\"areaChart\" was not injected: check your FXML file 'EngineLoadResult.fxml'.";
        assert xAxis != null : "fx:id=\"xAxis\" was not injected: check your FXML file 'EngineLoadResult.fxml'.";
        assert numberAxis != null : "fx:id=\"numberAxis\" was not injected: check your FXML file 'EngineLoadResult.fxml'.";

        initChart();

    }

    private void initChart() {
        stateToAxis.clear();
        for (WorkflowInstanceState workflowInstanceState : WorkflowInstanceState.values()) {
            XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
            series.setName(workflowInstanceState.toString());
            stateToAxis.put(workflowInstanceState, series);
            areaChart.getData().add(series);
        }
        // areaChart.getXAxis().setAnimated(false);
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("EngineLoadResult.fxml");
    }

    private static final int MAX_DATA_POINTS = 30;

    @Override
    public void showFilteredResult(List<WorkflowStateSummary> filteredlist, EngineLoadFilterModel usedFilter) {

        WorkflowStateSummary copperLoadInfo = filteredlist.get(0);
        Date date = new Date();
        // new SimpleDateFormat("HH:mm:ss").format(new Date());

        for (Entry<WorkflowInstanceState, Integer> entry : copperLoadInfo.getNumberOfWorkflowInstancesWithState().entrySet()) {
            Series<Number, Number> axis = stateToAxis.get(entry.getKey());
            ObservableList<Data<Number, Number>> data = axis.getData();
            data.add(new XYChart.Data<Number, Number>(date.getTime(), entry.getValue()));
            if (data.size() > MAX_DATA_POINTS) {
                data.remove(0);
            }

            if (usedFilter.stateFilters.get(entry.getKey()).getValue()) {
                if (!areaChart.getData().contains(axis)) {
                    areaChart.getData().add(axis);
                }
            } else {
                areaChart.getData().remove(axis);
            }
        }

        ComponentUtil.setupXAxis((NumberAxis) areaChart.getXAxis(), areaChart.getData());
    }

    @Override
    public List<WorkflowStateSummary> applyFilterInBackgroundThread(EngineLoadFilterModel filter) {
        return Arrays.asList(copperDataProvider.getCopperLoadInfo(filter.selectedEngine.getValue()));
    }

    @Override
    public boolean supportsClear() {
        return true;
    }

    @Override
    public void clear() {
        areaChart.getData().clear();
        initChart();
    }
}
