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
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.CheckBox;
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
import de.scoopgmbh.copper.monitoring.core.model.MeasurePointData;

public class CustomMeasurePointResultController extends FilterResultControllerBase<CustomMeasurePointFilterModel,MeasurePointData> implements Initializable {
	private final GuiCopperDataProvider copperDataProvider;
	private ArrayList<LogsRowModel> logEvents = new ArrayList<LogsRowModel>();
	
	public CustomMeasurePointResultController(GuiCopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}
	
	private static final int DEFAUKKT_TIME_FRAME = 1000*10;


    @FXML //  fx:id="logText"
    private TextArea logText; // Value injected by FXMLLoader

    @FXML //  fx:id="measurePointsChart"
    private LineChart<Number, Number> measurePointsChart; // Value injected by FXMLLoader

    @FXML //  fx:id="numberAxis"
    private NumberAxis numberAxis; // Value injected by FXMLLoader

    @FXML //  fx:id="ressourceChart"
    private AreaChart<Number, Number> ressourceChart; // Value injected by FXMLLoader

    @FXML //  fx:id="timeAxis"
    private NumberAxis timeAxis; // Value injected by FXMLLoader
    
    @FXML //  fx:id="timeResAxis"
    private NumberAxis timeResAxis;

    @FXML //  fx:id="measurePointField"
    private TextField measurePointField; // Value injected by FXMLLoader

    @FXML //  fx:id="timeRange"
    private TextField timeRange; // Value injected by FXMLLoader

    @FXML //  fx:id="pointsButton"
    private RadioButton pointsButton; // Value injected by FXMLLoader

    @FXML //  fx:id="quantilButton"
    private CheckBox quantilButton; // Value injected by FXMLLoader

    @FXML //  fx:id="textButton"
    private RadioButton textButton; // Value injected by FXMLLoader

    @FXML //  fx:id="textChart"
    private TextArea textChart; // Value injected by FXMLLoader
	private Series<Number, Number> series99qantil = new Series<Number, Number>();
	private Series<Number, Number> series90qantil = new Series<Number, Number>();
	private Series<Number, Number> series50qantil = new Series<Number, Number>();
	private Map<String, List<MeasurePointData>> groups;
	private Series<Number, Number> measurePointSeries;



    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert logText != null : "fx:id=\"logText\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert measurePointField != null : "fx:id=\"measurePointField\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert measurePointsChart != null : "fx:id=\"measurePointsChart\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert numberAxis != null : "fx:id=\"numberAxis\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert pointsButton != null : "fx:id=\"pointsButton\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert quantilButton != null : "fx:id=\"quantilButton\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
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
        pointsButton.setToggleGroup(group);
        textButton.setToggleGroup(group);
        measurePointsChart.visibleProperty().bind(pointsButton.selectedProperty());
        textChart.visibleProperty().bind(textButton.selectedProperty());
        
        pointsButton.setSelected(true);
        
        measurePointSeries = new Series<Number, Number>();
        measurePointsChart.getData().add(measurePointSeries);
        
		series99qantil.setName("99%");
		series90qantil.setName("90%");
		series50qantil.setName("50%");
		measurePointsChart.getData().add(series99qantil);
		measurePointsChart.getData().add(series90qantil);
		measurePointsChart.getData().add(series50qantil);
		
		
		quantilButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue!=null){
					updateQuantilSeries(newValue);
				}
			}
		});

    }
    
	private void updateQuantilSeries(boolean visible) {
		measurePointsChart.setAnimated(false);
		if (!visible) {
			series50qantil.getData().clear();
			series90qantil.getData().clear();
			series99qantil.getData().clear();
		} else {
			craeteQuantil(groups);
		}
		measurePointsChart.setAnimated(true);
	}
	
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("CustomMeasurePointResult.fxml");
	}

	@Override
	public void showFilteredResult(List<MeasurePointData> filteredlist, CustomMeasurePointFilterModel usedFilter) {
		groups = new HashMap<String, List<MeasurePointData>>();
		for (MeasurePointData measurePointData: filteredlist){
			List<MeasurePointData> group = groups.get(measurePointData.getMeasurePointId());
			if (group==null){
				group= new ArrayList<MeasurePointData>();
				groups.put(measurePointData.getMeasurePointId(),group);
			}
			group.add(measurePointData);
		}
		craetePointAndRessourceChart(groups);
		craeteTextChart(groups);;
		setupChart(groups);
	}
	
	private void craeteTextChart(Map<String, List<MeasurePointData>> groups) {
		StringBuilder result = new StringBuilder();
		for (List<MeasurePointData> group: groups.values()){
			result.append(group.get(0).getMeasurePointId()+"\n");
			for (MeasurePointData measurePointData: group){
				result.append(measurePointData.getTime()+"\t"+measurePointData.getElapsedTimeMicros()+"\n");
			}
		}
		textChart.setText(result.toString());
	}
	
	private void setupChart(Map<String, List<MeasurePointData>> groups) {
		long xmin=Long.MAX_VALUE;
		long xmax=Long.MIN_VALUE;
		long ymax=Long.MIN_VALUE;
		
		for (List<MeasurePointData> group: groups.values()){
			for (MeasurePointData measurePointData: group){
				final long occurence = measurePointData.getTime().getTime();
				final long elapsedTime = measurePointData.getElapsedTimeMicros();

				xmin = Math.min(xmin, occurence);
				xmax = Math.max(xmax, occurence);
				ymax = Math.max(ymax, elapsedTime);
			}
		}
		
		measurePointsChart.getYAxis().setAutoRanging(false);
		((NumberAxis)measurePointsChart.getYAxis()).setLowerBound(0);
		((NumberAxis)measurePointsChart.getYAxis()).setUpperBound(ymax*1.10);
		
		ComponentUtil.setupXAxis(timeAxis,xmin,xmax);
		ComponentUtil.setupXAxis(timeResAxis,xmin,xmax);
	}
	
	private void craeteQuantil(Map<String, List<MeasurePointData>> groups) {
		series99qantil.getData().clear();
		series90qantil.getData().clear();
		series50qantil.getData().clear();
		
		ArrayList<Long> data = new ArrayList<Long>();
		for (List<MeasurePointData> group: groups.values()){
			for (MeasurePointData measurePointData: group){
				final long elapsedTime = measurePointData.getElapsedTimeMicros();
				data.add(elapsedTime);
			}
		}
		
		final Long[] sorted = data.toArray(new Long[data.size()]);
		Arrays.sort(sorted);
		long qu50 = sorted[sorted.length/2];
		long qu90 = sorted[sorted.length*9/10];
		long qu99 = sorted[sorted.length*99/100];
		
		for (List<MeasurePointData> group: groups.values()){
			for (int i=0;i<group.size();i++){
				if (i==0 || i==group.size()-1){
					final long occurence = group.get(i).getTime().getTime();
					final Data<Number, Number> data99 = new XYChart.Data<Number, Number>(occurence, qu99);
					data99.setNode(new HoveredNode(occurence,qu99,""));
					series99qantil.getData().add(data99);
					final Data<Number, Number> data90 = new XYChart.Data<Number, Number>(occurence, qu90);
					data90.setNode(new HoveredNode(occurence,qu90,""));
					series90qantil.getData().add(data90);
					final Data<Number, Number> data50 = new XYChart.Data<Number, Number>(occurence, qu50);
					data50.setNode(new HoveredNode(occurence,qu50,""));
					series50qantil.getData().add(data50);
				}
			}
		}
	}
	
	private void craetePointAndRessourceChart(Map<String, List<MeasurePointData>> groups) {
		measurePointSeries.getData().clear();
		ressourceChart.getData().clear();
		
		updateQuantilSeries(quantilButton.isSelected());
		
		Series<Number, Number> systemCPU = new Series<Number, Number>();
		systemCPU.setName("System CPU usage");
		Series<Number, Number> processCPU = new Series<Number, Number>();
		processCPU.setName("Process CPU usage");
		for (List<MeasurePointData> group: groups.values()){
			measurePointSeries.setName(group.get(0).getMeasurePointId());
			ObservableList<Data<Number, Number>> data = measurePointSeries.getData();
			for (MeasurePointData measurePointData: group){
				final long time = measurePointData.getTime().getTime();
				final Data<Number, Number> dataPoint = new XYChart.Data<Number, Number>(time, measurePointData.getElapsedTimeMicros());
				dataPoint.setNode(new HoveredNode(time,measurePointData.getElapsedTimeMicros(),measurePointData.getMeasurePointId()+", "+measurePointData.getTime()));
				data.add(dataPoint);
				if (ressourceChart.getData().isEmpty()){
					systemCPU.getData().add(new XYChart.Data<Number, Number>(time, measurePointData.getSystemResourcesInfo().getSystemCpuLoad()));
					processCPU.getData().add(new XYChart.Data<Number, Number>(time, measurePointData.getSystemResourcesInfo().getProcessCpuLoad()));
				}
			}
			
			if (ressourceChart.getData().isEmpty()){
				ressourceChart.getData().add(systemCPU);
				ressourceChart.getData().add(processCPU);
			}
		}
	}
	
	class HoveredNode extends StackPane {
		long value;
		public HoveredNode(final long xvalue, final long yvalue, final String measurePointDescription) {
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
	public List<MeasurePointData> applyFilterInBackgroundThread(CustomMeasurePointFilterModel filter) {
		logEvents.clear();
		logEvents.addAll(copperDataProvider.getLogData().logs);
		
		return copperDataProvider.getMonitoringMeasurePoints(filter,this.maxResultCountProperty().get());
	}

	@Override
	public boolean canLimitResult() {
		return true;
	}
	
	@Override
	public void clear() {
		measurePointsChart.getData().clear();
		ressourceChart.getData().clear();
		logText.clear();
	}
}
