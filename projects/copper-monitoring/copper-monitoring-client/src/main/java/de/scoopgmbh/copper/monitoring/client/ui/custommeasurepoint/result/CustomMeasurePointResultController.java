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
package de.scoopgmbh.copper.monitoring.client.ui.custommeasurepoint.result;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.util.StringConverter;
import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultControllerBase;
import de.scoopgmbh.copper.monitoring.client.ui.custommeasurepoint.filter.CustomMeasurePointFilterModel;
import de.scoopgmbh.copper.monitoring.core.model.MeasurePointData;

public class CustomMeasurePointResultController extends FilterResultControllerBase<CustomMeasurePointFilterModel,MeasurePointData> implements Initializable {
	private final GuiCopperDataProvider copperDataProvider;
	
	public CustomMeasurePointResultController(GuiCopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}

    @FXML //  fx:id="areaChart"
    private AreaChart<Long, Number> areaChart; // Value injected by FXMLLoader

    @FXML //  fx:id="categoryAxis"
    private NumberAxis timeAxis; // Value injected by FXMLLoader

    @FXML //  fx:id="numberAxis"
    private NumberAxis numberAxis; // Value injected by FXMLLoader

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert areaChart != null : "fx:id=\"areaChart\" was not injected: check your FXML file 'EngineLoadResult.fxml'.";
        assert timeAxis != null : "fx:id=\"categoryAxis\" was not injected: check your FXML file 'EngineLoadResult.fxml'.";
        assert numberAxis != null : "fx:id=\"numberAxis\" was not injected: check your FXML file 'EngineLoadResult.fxml'.";
        maxResultCountProperty().set(100);
    }
	
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("CustomMeasurePointResult.fxml");
	}

	@Override
	public void showFilteredResult(List<MeasurePointData> filteredlist, CustomMeasurePointFilterModel usedFilter) {
		
		Map<String, List<MeasurePointData>> groups = new HashMap<String, List<MeasurePointData>>();
		for (MeasurePointData measurePointData: filteredlist){
			List<MeasurePointData> group = groups.get(measurePointData.getMeasurePointId());
			if (group==null){
				group= new ArrayList<MeasurePointData>();
				groups.put(measurePointData.getMeasurePointId(),group);
			}
			group.add(measurePointData);
		}
		
		long min=Long.MAX_VALUE;
		long max=Long.MIN_VALUE;
		areaChart.getData().clear();
		for (List<MeasurePointData> group: groups.values()){
			Series<Long, Number> axis = new Series<Long, Number>();
			axis.setName(group.get(0).getMeasurePointId());
			ObservableList<Data<Long, Number>> data = axis.getData();
			for (MeasurePointData measurePointData: group){
				min = Math.min(min, measurePointData.getTime().getTime());
				max = Math.max(max, measurePointData.getTime().getTime());
				data.add(new XYChart.Data<Long, Number>(measurePointData.getTime().getTime(), measurePointData.getElapsedTimeMicros()));
			}
			areaChart.getData().add(axis);
		}
		timeAxis.setAutoRanging(false);
		timeAxis.setTickUnit((max-min)/20);
		timeAxis.setLowerBound(min);
		timeAxis.setUpperBound(max);
		timeAxis.setTickLabelFormatter(new StringConverter<Number>() {
			private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy\nHH:mm:ss,SSS");
			@Override
			public String toString(Number object) {
				return format.format(new Date(object.longValue()));
			}
			
			@Override
			public Number fromString(String string) {
				return null;
			}
		});
	}

	@Override
	public List<MeasurePointData> applyFilterInBackgroundThread(CustomMeasurePointFilterModel filter) {
		return copperDataProvider.getMonitoringMeasurePoints(filter,this.maxResultCountProperty().get());
	}

	@Override
	public boolean canLimitResult() {
		return true;
	}
	
	@Override
	public void clear() {
		areaChart.getData().clear();
	}
}
