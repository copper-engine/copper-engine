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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultControllerBase;
import de.scoopgmbh.copper.monitoring.client.ui.custommeasurepoint.filter.CustomMeasurePointFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.logs.result.LogsResultModel.LogsRowModel;
import de.scoopgmbh.copper.monitoring.client.util.ComponentUtil;
import de.scoopgmbh.copper.monitoring.core.statistic.TimeValuePair;

public class CustomMeasurePointResultController extends FilterResultControllerBase<CustomMeasurePointFilterModel,CustomMeasurePointResultModel> implements Initializable {
	private final GuiCopperDataProvider copperDataProvider;
	private ArrayList<LogsRowModel> logEvents = new ArrayList<LogsRowModel>();
	
	public CustomMeasurePointResultController(GuiCopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}
	
	private static final int DEFAUKKT_TIME_FRAME = 1000*10;



    @FXML //  fx:id="avgButton"
    private RadioButton avgButton; // Value injected by FXMLLoader

    @FXML //  fx:id="avgChart"
    private LineChart<Number, Number> avgChart; // Value injected by FXMLLoader

    @FXML //  fx:id="countButton"
    private RadioButton countButton; // Value injected by FXMLLoader

    @FXML //  fx:id="countChart"
    private LineChart<Number, Number> countChart; // Value injected by FXMLLoader

    @FXML //  fx:id="logText"
    private TextArea logText; // Value injected by FXMLLoader

    @FXML //  fx:id="measurePointField"
    private TextField measurePointField; // Value injected by FXMLLoader

    @FXML //  fx:id="numberAxis"
    private NumberAxis numberAxis; // Value injected by FXMLLoader

    @FXML //  fx:id="quantilButton"
    private RadioButton quantilButton; // Value injected by FXMLLoader

    @FXML //  fx:id="quantilChart"
    private LineChart<Number, Number> quantilChart; // Value injected by FXMLLoader

    @FXML //  fx:id="ressourceChart"
    private AreaChart<Number, Number> ressourceChart; // Value injected by FXMLLoader

    @FXML //  fx:id="textButton"
    private RadioButton textButton; // Value injected by FXMLLoader

    @FXML //  fx:id="textChart"
    private TextArea textChart; // Value injected by FXMLLoader

    @FXML //  fx:id="timeAxis"
    private NumberAxis timeAxis; // Value injected by FXMLLoader

    @FXML //  fx:id="timeRange"
    private TextField timeRange; // Value injected by FXMLLoader

    @FXML //  fx:id="timeResAxis"
    private NumberAxis timeResAxis; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert avgButton != null : "fx:id=\"avgButton\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert avgChart != null : "fx:id=\"avgChart\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert countButton != null : "fx:id=\"countButton\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert countChart != null : "fx:id=\"countChart\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert logText != null : "fx:id=\"logText\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert measurePointField != null : "fx:id=\"measurePointField\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert numberAxis != null : "fx:id=\"numberAxis\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert quantilButton != null : "fx:id=\"quantilButton\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert quantilChart != null : "fx:id=\"quantilChart\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert ressourceChart != null : "fx:id=\"ressourceChart\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert textButton != null : "fx:id=\"textButton\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert textChart != null : "fx:id=\"textChart\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert timeAxis != null : "fx:id=\"timeAxis\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert timeRange != null : "fx:id=\"timeRange\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert timeResAxis != null : "fx:id=\"timeResAxis\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        
        maxResultCountProperty().set(100);
        logText.setWrapText(false);
        logText.getStyleClass().add("consoleFont");
        timeRange.setText(""+DEFAUKKT_TIME_FRAME);
       
        final ToggleGroup group = new ToggleGroup();
        avgButton.setToggleGroup(group);
        textButton.setToggleGroup(group);
        countButton.setToggleGroup(group);
        quantilButton.setToggleGroup(group);
        avgChart.visibleProperty().bind(avgButton.selectedProperty());
        textChart.visibleProperty().bind(textButton.selectedProperty());
        countChart.visibleProperty().bind(countButton.selectedProperty());
        quantilChart.visibleProperty().bind(quantilButton.selectedProperty());
        
        avgButton.setSelected(true);
        
    }
    
	private void updateChart(XYChart<Number, Number> chart, Map<String,List<TimeValuePair<Double>>> seriesTitleToData) {
		chart.getData().clear();
		
		for (Entry<String,List<TimeValuePair<Double>>> seriesData: seriesTitleToData.entrySet()){
			final Series<Number, Number> series = new Series<Number, Number>();
			series.setName(seriesData.getKey());
			for (TimeValuePair<Double> timeValuePair: seriesData.getValue()){
				final Data<Number, Number> data = new Data<Number, Number>(timeValuePair.date.getTime(),timeValuePair.value);
				data.setNode(new HoveredNode(timeValuePair.date.getTime(),timeValuePair.value,""));
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
	public URL getFxmlRessource() {
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
			map.put("50% quantil",measurePointResultModel.quantil50);
			map.put("90% quantil",measurePointResultModel.quantil90);
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
			result.append(seriesData.getKey()+"\n");
			for (TimeValuePair<Double> value: seriesData.getValue()){
				result.append(value.date+"\t"+value.value+"\n");
			}
		}
		textChart.setText(result.toString());
	}
	
//	private void craetePointAndRessourceChart() {
//		measurePointSeries.getData().clear();
//		ressourceChart.getData().clear();
//		
//		updateQuantilSeries(quantilButton.isSelected());
//		
//		Series<Number, Number> systemCPU = new Series<Number, Number>();
//		systemCPU.setName("System CPU usage");
//		Series<Number, Number> processCPU = new Series<Number, Number>();
//		processCPU.setName("Process CPU usage");
//		for (List<MeasurePointData> group: groups.values()){
//			measurePointSeries.setName(group.get(0).getMeasurePointId());
//			ObservableList<Data<Number, Number>> data = measurePointSeries.getData();
//			for (MeasurePointData measurePointData: group){
//				final long time = measurePointData.getTime().getTime();
//				final Data<Number, Number> dataPoint = new XYChart.Data<Number, Number>(time, measurePointData.getElapsedTimeMicros());
//				dataPoint.setNode(new HoveredNode(time,measurePointData.getElapsedTimeMicros(),measurePointData.getMeasurePointId()+", "+measurePointData.getTime()));
//				data.add(dataPoint);
//				if (ressourceChart.getData().isEmpty()){
//					systemCPU.getData().add(new XYChart.Data<Number, Number>(time, measurePointData.getSystemResourcesInfo().getSystemCpuLoad()));
//					processCPU.getData().add(new XYChart.Data<Number, Number>(time, measurePointData.getSystemResourcesInfo().getProcessCpuLoad()));
//				}
//			}
//			
//			if (ressourceChart.getData().isEmpty()){
//				ressourceChart.getData().add(systemCPU);
//				ressourceChart.getData().add(processCPU);
//			}
//		}
//	}
	
	class HoveredNode extends StackPane {
		public HoveredNode(final long xvalue, final double yvalue, final String measurePointDescription) {
			setPrefSize(9, 9);
			setOnMouseEntered(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent mouseEvent) {
					logText.clear();
					long timerangeTmp = DEFAUKKT_TIME_FRAME;
					try {
						timerangeTmp = Long.valueOf(timeRange.getText());
						
					} catch(NumberFormatException e){
						//ignore
					}
					StringBuilder builder = new StringBuilder();
					int counter=0;;
					for (LogsRowModel logsRowModel: logEvents){
						if (logsRowModel.time.get().getTime() > xvalue-timerangeTmp  && 
							logsRowModel.time.get().getTime() < xvalue+timerangeTmp){
							builder.append(logsRowModel.toString());
							counter++;
							if (counter>1000){
								builder.append("... aborted after 1000 ....");
								break;
							}
						}
					};
					measurePointField.setText(measurePointDescription);
					logText.setText(builder.toString());
					
					
					setMinSize(50, 20);
					final TextField textField = new TextField(""+yvalue);
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
		logEvents.clear();
		logEvents.addAll(copperDataProvider.getLogData(null,null,this.maxResultCountProperty().get()).logs);
		
		return Arrays.asList(copperDataProvider.getMonitoringMeasurePoints(filter,null,null));
	}

	@Override
	public boolean canLimitResult() {
		return true;
	}
	
	@Override
	public void clear() {
		 avgChart.getData().clear();
		 countChart.getData().clear();
		 quantilChart.getData().clear();
		 logText.clear();
	}
}
