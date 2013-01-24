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
package de.scoopgmbh.copper.gui.ui.measurepoint.result;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterResultController;
import de.scoopgmbh.copper.gui.util.EngineFilter;
import de.scoopgmbh.copper.monitor.adapter.model.MeasurePointData;

public class MeasurePointResultController implements Initializable, FilterResultController<EngineFilter,MeasurePointData>, FxmlController {
	private final GuiCopperDataProvider copperDataProvider;
	
	public MeasurePointResultController(GuiCopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}

    @FXML //  fx:id="chart"
    private BarChart<String, Number> chart; // Value injected by FXMLLoader

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert chart != null : "fx:id=\"chart\" was not injected: check your FXML file 'MeasurePointResult.fxml'.";

        initChart();
    }

	private void initChart() {
		axis = new XYChart.Series<>();
		axis.setName("Measuring points");
		chart.getData().add(axis);
		chart.getYAxis().setLabel("average micro secounds"); 
	}
	
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("MeasurePointResult.fxml");
	}

	private XYChart.Series<String, Number> axis;
	
	@Override
	public void showFilteredResult(List<MeasurePointData> filteredlist, EngineFilter usedFilter) {
		clear();
		for (MeasurePointData measurePointData: filteredlist){
			ObservableList<Data<String, Number>> data = axis.getData();
			data.add(new XYChart.Data<String, Number>(measurePointData.getMpId(), measurePointData.getElapsedTimeMicros()/measurePointData.getCount()));
		}
	}
	
	@Override
	public List<MeasurePointData> applyFilterInBackgroundThread(EngineFilter filter) {
		return copperDataProvider.getMeasurePoints(filter);
	}

	@Override
	public boolean canLimitResult() {
		return false;
	}
	
	@Override
	public void clear() {
		axis.getData().clear();
	}
}
