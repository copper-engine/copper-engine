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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultControllerBase;
import de.scoopgmbh.copper.monitoring.client.form.filter.defaultfilter.DefaultFilterFactory;
import de.scoopgmbh.copper.monitoring.client.ui.custommeasurepoint.filter.CustomMeasurePointFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.logs.result.LogsResultModel.LogsRowModel;
import de.scoopgmbh.copper.monitoring.client.util.ComponentUtil;
import de.scoopgmbh.copper.monitoring.core.statistic.TimeValuePair;

public class CustomMeasurePointResultController extends FilterResultControllerBase<CustomMeasurePointFilterModel,CustomMeasurePointResultModel> implements Initializable {
	private final GuiCopperDataProvider copperDataProvider;

	
	public CustomMeasurePointResultController(GuiCopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}
	
	private static final int DEFAULT_TIME_FRAME = 1000*10;


    @FXML //  fx:id="avgChart"
    private LineChart<Number, Number> avgChart; // Value injected by FXMLLoader

    @FXML //  fx:id="countChart"
    private LineChart<Number, Number> countChart; // Value injected by FXMLLoader

    @FXML //  fx:id="logText"
    private TextArea logText; // Value injected by FXMLLoader

    @FXML //  fx:id="measurePointField"
    private TextField measurePointField; // Value injected by FXMLLoader

    @FXML //  fx:id="numberAxis"
    private NumberAxis numberAxis; // Value injected by FXMLLoader

    @FXML //  fx:id="quantilChart"
    private LineChart<Number, Number> quantilChart; // Value injected by FXMLLoader

    @FXML //  fx:id="ressourceChart"
    private AreaChart<Number, Number> ressourceChart; // Value injected by FXMLLoader

    @FXML //  fx:id="textChart"
    private TextArea textChart; // Value injected by FXMLLoader

    @FXML //  fx:id="timeAxis"
    private NumberAxis timeAxis; // Value injected by FXMLLoader

    @FXML //  fx:id="timeRange"
    private TextField timeRange; // Value injected by FXMLLoader

    @FXML //  fx:id="timeResAxis"
    private NumberAxis timeResAxis; // Value injected by FXMLLoader
    
    @FXML //  fx:id="updateLog"
    private Button updateLog; // Value injected by FXMLLoader
    
    @FXML //  fx:id="logWrapStackpane"
    private StackPane logWrapStackpane; // Value injected by FXMLLoader

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert avgChart != null : "fx:id=\"avgChart\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert countChart != null : "fx:id=\"countChart\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert logText != null : "fx:id=\"logText\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert measurePointField != null : "fx:id=\"measurePointField\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert numberAxis != null : "fx:id=\"numberAxis\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert quantilChart != null : "fx:id=\"quantilChart\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert ressourceChart != null : "fx:id=\"ressourceChart\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert textChart != null : "fx:id=\"textChart\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert timeAxis != null : "fx:id=\"timeAxis\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert timeRange != null : "fx:id=\"timeRange\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert timeResAxis != null : "fx:id=\"timeResAxis\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert updateLog != null : "fx:id=\"updateLog\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert logWrapStackpane != null : "fx:id=\"logWrapStackpane\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        
        logText.setWrapText(false);
        logText.getStyleClass().add("consoleFont");
        timeRange.setText(""+DEFAULT_TIME_FRAME);
        
        selectedPoint.addListener(new ChangeListener<TimeValuePair<Double>>() {

			@Override
			public void changed(ObservableValue<? extends TimeValuePair<Double>> observable, TimeValuePair<Double> oldValue,
					TimeValuePair<Double> newValue) {
				if (newValue!=null){
					measurePointField.setText(new SimpleDateFormat(DefaultFilterFactory.DATE_FORMAT).format(newValue.date)+" , value: "+newValue.value+"μs");
				}
			}
		});
        
        updateLog.disableProperty().bind(selectedPoint.isNull());
        updateLog.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ComponentUtil.executeWithProgressDialogInBackground(new Runnable() {
					@Override
					public void run() {
						long timerangeTmp = DEFAULT_TIME_FRAME;
						try {
							timerangeTmp = Long.valueOf(timeRange.getText());
						} catch(NumberFormatException e){
							//ignore
						}
						List<LogsRowModel> logEvents = copperDataProvider.getLogData(
								new Date(selectedPoint.get().date.getTime()-timerangeTmp),
								new Date(selectedPoint.get().date.getTime()+timerangeTmp),1000).logs;
						final StringBuilder builder = new StringBuilder();
						int counter=0;
						for (LogsRowModel logsRowModel: logEvents){
								builder.append(logsRowModel.toString());
								counter++;
								if (counter>1000){
									builder.append("... aborted after 1000 ....");
									break;
								}
						}
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								logText.setText(builder.toString());
							}
						});
					}
				}, logWrapStackpane, "");
			}
		});
    }
    
	private void updateChart(XYChart<Number, Number> chart, Map<String,List<TimeValuePair<Double>>> seriesTitleToData) {
		chart.getData().clear();
		
		for (Entry<String,List<TimeValuePair<Double>>> seriesData: seriesTitleToData.entrySet()){
			final Series<Number, Number> series = new Series<Number, Number>();
			series.setName(seriesData.getKey());
			for (TimeValuePair<Double> timeValuePair: seriesData.getValue()){
				final Data<Number, Number> data = new Data<Number, Number>(timeValuePair.date.getTime(),timeValuePair.value);
				data.setNode(new HoveredNode(timeValuePair));
				series.getData().add(data);
			}
			chart.getData().add(series);
		}
		ComponentUtil.setupXAxis((NumberAxis)chart.getXAxis(),chart.getData());
		
//		measurePointsChart.getYAxis().setAutoRanging(false);
//		((NumberAxis)measurePointsChart.getYAxis()).setLowerBound(0);
//		((NumberAxis)measurePointsChart.getYAxis()).setUpperBound(ymax*1.10);
	}
	
	@Override
	public URL getFxmlResource() {
		return getClass().getResource("CustomMeasurePointResult.fxml");
	}

	@Override
	public void showFilteredResult(List<CustomMeasurePointResultModel> filteredlist, CustomMeasurePointFilterModel usedFilter) {
		CustomMeasurePointResultModel measurePointResultModel = filteredlist.get(0);
		
		Map<String,List<TimeValuePair<Double>>> seriesTitleToDataForText = new HashMap<String,List<TimeValuePair<Double>>>();
		
		{
			HashMap<String,List<TimeValuePair<Double>>> map = new HashMap<String,List<TimeValuePair<Double>>>();
			map.put("average",measurePointResultModel.avg);
			updateChart(avgChart,map);
			seriesTitleToDataForText.putAll(map);
		}
		{
			HashMap<String,List<TimeValuePair<Double>>> map = new HashMap<String,List<TimeValuePair<Double>>>();
			map.put("count",measurePointResultModel.count);
			updateChart(countChart,map);
			seriesTitleToDataForText.putAll(map);
		}
		{
			HashMap<String,List<TimeValuePair<Double>>> map = new HashMap<String,List<TimeValuePair<Double>>>();
			map.put("90% quantil",measurePointResultModel.quantil90);
			map.put("50% quantil",measurePointResultModel.quantil50);
			map.put("99% quantil",measurePointResultModel.quantil99);
			updateChart(quantilChart,map);
			seriesTitleToDataForText.putAll(map);
		}
		{
			HashMap<String,List<TimeValuePair<Double>>> map = new HashMap<String,List<TimeValuePair<Double>>>();
			map.put("system cpu load",measurePointResultModel.avgCpuCreator);
			updateChart(ressourceChart,map);
			seriesTitleToDataForText.putAll(map);
		}
		
		craeteTextChart(seriesTitleToDataForText);
	}
	
	private void craeteTextChart(Map<String,List<TimeValuePair<Double>>> seriesTitleToData) {
		StringBuilder result = new StringBuilder();
		for (Entry<String,List<TimeValuePair<Double>>> seriesData: seriesTitleToData.entrySet()){
			result.append(seriesData.getKey());
            result.append("\n");
			for (TimeValuePair<Double> value: seriesData.getValue()){
				result.append(value.date);
                result.append("\t");
                result.append(value.value);
                result.append("\n");
			}
		}
		textChart.setText(result.toString());
	}
	
	public SimpleObjectProperty<TimeValuePair<Double>> selectedPoint = new SimpleObjectProperty<TimeValuePair<Double>>();
	
	class HoveredNode extends StackPane {
		public HoveredNode(final TimeValuePair<Double> value) {
			setPrefSize(9, 9);
			setOnMouseEntered(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent mouseEvent) {
					selectedPoint.set(value);
					
					setMinSize(50, 20);
					final TextField textField = new TextField(""+value.value);
					textField.setEditable(false);
					getChildren().add(textField);
					textField.minWidthProperty().bind(minWidthProperty());
					textField.minHeightProperty().bind(minHeightProperty());
				}
			});
			setOnMouseExited(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent mouseEvent) {
					getChildren().clear();
					setPrefSize(9, 9);
				}
			});
		}
	}

	@Override
	public List<CustomMeasurePointResultModel> applyFilterInBackgroundThread(CustomMeasurePointFilterModel filter) {

		return Arrays.asList(copperDataProvider.getMonitoringMeasurePoints(
				filter,filter.fromToFilterModel.from.get(),filter.fromToFilterModel.to.get(),filter.maxCountFilterModel.getMaxCount()));
	}
	
	@Override
	public void clear() {
		 avgChart.getData().clear();
		 countChart.getData().clear();
		 quantilChart.getData().clear();
		 ressourceChart.getData().clear();
		 logText.clear();
	}
}
