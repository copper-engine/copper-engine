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
package de.scoopgmbh.copper.monitoring.client.ui.measurepoint.result;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultControllerBase;
import de.scoopgmbh.copper.monitoring.client.form.filter.enginefilter.EnginePoolFilterModel;
import de.scoopgmbh.copper.monitoring.core.model.MeasurePointData;

public class MeasurePointResultController extends FilterResultControllerBase<EnginePoolFilterModel, MeasurePointData> implements Initializable {
    private final GuiCopperDataProvider copperDataProvider;

    public MeasurePointResultController(GuiCopperDataProvider copperDataProvider) {
        super();
        this.copperDataProvider = copperDataProvider;
    }

    @FXML
    // fx:id="chart"
    private BarChart<String, Number> chart; // Value injected by FXMLLoader

    @FXML
    // fx:id="reset"
    private Button reset; // Value injected by FXMLLoader

    @Override
    // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert chart != null : "fx:id=\"chart\" was not injected: check your FXML file 'MeasurePointResult.fxml'.";
        assert reset != null : "fx:id=\"reset\" was not injected: check your FXML file 'MeasurePointResult.fxml'.";

        reset.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                copperDataProvider.resetMeasurePoints();
                clear();
            }
        });
        reset.getStyleClass().add("copperActionButton");

        initChart();
    }

    private void initChart() {
        axis = new XYChart.Series<String, Number>();
        axis.setName("Measuring points");
        chart.getData().add(axis);
        chart.getYAxis().setLabel("average micro seconds");
    }

    @Override
    public URL getFxmlResource() {
        return getClass().getResource("MeasurePointResult.fxml");
    }

    private XYChart.Series<String, Number> axis;

    @Override
    public void showFilteredResult(List<MeasurePointData> filteredlist, EnginePoolFilterModel usedFilter) {
        clear();
        for (MeasurePointData measurePointData : filteredlist) {
            ObservableList<Data<String, Number>> data = axis.getData();
            if (measurePointData.getCount() != 0) {
                String text = measurePointData.getMeasurePointId();
                if (text != null) {
                    text = text.replace("de.scoopgmbh.copper.persistent", "");
                }
                data.add(new XYChart.Data<String, Number>(text, measurePointData.getElapsedTimeMicros() / measurePointData.getCount()));
            }
        }
    }

    @Override
    public List<MeasurePointData> applyFilterInBackgroundThread(EnginePoolFilterModel filter) {
        return copperDataProvider.getMeasurePoints(filter);
    }

    @Override
    public boolean supportsClear() {
        return true;
    }

    @Override
    public void clear() {
        axis.getData().clear();
    }
}
