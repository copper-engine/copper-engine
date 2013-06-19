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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultControllerBase;
import de.scoopgmbh.copper.monitoring.client.ui.custommeasurepoint.filter.CustomMeasurePointFilterModel;
import de.scoopgmbh.copper.monitoring.client.ui.logs.result.LogsResultModel.LogsRowModel;
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
    private AreaChart<Long, Number> measurePointsChart; // Value injected by FXMLLoader

    @FXML //  fx:id="numberAxis"
    private NumberAxis numberAxis; // Value injected by FXMLLoader

    @FXML //  fx:id="ressourceChart"
    private AreaChart<Long, Number> ressourceChart; // Value injected by FXMLLoader

    @FXML //  fx:id="timeAxis"
    private NumberAxis timeAxis; // Value injected by FXMLLoader
    
    @FXML //  fx:id="timeResAxis"
    private NumberAxis timeResAxis;

    @FXML //  fx:id="measurePointField"
    private TextField measurePointField; // Value injected by FXMLLoader

    @FXML //  fx:id="timeRange"
    private TextField timeRange; // Value injected by FXMLLoader

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert logText != null : "fx:id=\"logText\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert measurePointField != null : "fx:id=\"measurePointField\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert measurePointsChart != null : "fx:id=\"measurePointsChart\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert numberAxis != null : "fx:id=\"numberAxis\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert ressourceChart != null : "fx:id=\"ressourceChart\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert timeAxis != null : "fx:id=\"timeAxis\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert timeRange != null : "fx:id=\"timeRange\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";
        assert timeResAxis != null : "fx:id=\"timeResAxis\" was not injected: check your FXML file 'CustomMeasurePointResult.fxml'.";

        
        maxResultCountProperty().set(100);
        logText.setWrapText(false);
        logText.getStyleClass().add("consoleFont");
        timeRange.setText(""+DEFAUKKT_TIME_FRAME);
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
		measurePointsChart.getData().clear();
		ressourceChart.getData().clear();
		Series<Long, Number> systemCPU = new Series<Long, Number>();
		systemCPU.setName("System CPU usage");
		Series<Long, Number> processCPU = new Series<Long, Number>();
		processCPU.setName("Process CPU usage");
		for (List<MeasurePointData> group: groups.values()){
			Series<Long, Number> axis = new Series<Long, Number>();
			axis.setName(group.get(0).getMeasurePointId());
			ObservableList<Data<Long, Number>> data = axis.getData();
			for (MeasurePointData measurePointData: group){
				final long time = measurePointData.getTime().getTime();
				min = Math.min(min, time);
				max = Math.max(max, time);
				final Data<Long, Number> dataPoint = new XYChart.Data<Long, Number>(time, measurePointData.getElapsedTimeMicros());
				dataPoint.setNode(new HoveredNode(time,measurePointData.getMeasurePointId()+", "+measurePointData.getTime()));
				data.add(dataPoint);
				if (ressourceChart.getData().isEmpty()){
					systemCPU.getData().add(new XYChart.Data<Long, Number>(time, measurePointData.getSystemResourcesInfo().getSystemCpuLoad()));
					processCPU.getData().add(new XYChart.Data<Long, Number>(time, measurePointData.getSystemResourcesInfo().getProcessCpuLoad()));
				}
			}
			measurePointsChart.getData().add(axis);
			if (ressourceChart.getData().isEmpty()){
				ressourceChart.getData().add(systemCPU);
				ressourceChart.getData().add(processCPU);
			}
		}
		
		setupXAxis(timeAxis,min,max);
		setupXAxis(timeResAxis,min,max);
	}
	
	private void setupXAxis(NumberAxis numberAxis, long min, long max ){
		numberAxis.setAutoRanging(false);
		numberAxis.setTickUnit((max-min)/20);
		numberAxis.setLowerBound(min);
		numberAxis.setUpperBound(max);
		numberAxis.setTickLabelFormatter(new StringConverter<Number>() {
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
	
	class HoveredNode extends StackPane {
		long value;
		public HoveredNode(final long value, final String measurePointDescription) {
			setPrefSize(7, 7);
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
						if (logsRowModel.time.get().getTime() > value-timerangeTmp  && 
							logsRowModel.time.get().getTime() < value+timerangeTmp){
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
				}
			});
			setOnMouseExited(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent mouseEvent) {
					
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
