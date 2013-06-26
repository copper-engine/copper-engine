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
package de.scoopgmbh.copper.monitoring.client.ui.systemresource.result;

import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultControllerBase;
import de.scoopgmbh.copper.monitoring.client.ui.systemresource.filter.ResourceFilterModel;
import de.scoopgmbh.copper.monitoring.client.util.ComponentUtil;
import de.scoopgmbh.copper.monitoring.core.model.SystemResourcesInfo;

public class RessourceResultController extends FilterResultControllerBase<ResourceFilterModel,SystemResourcesInfo> implements Initializable {
	private final GuiCopperDataProvider copperDataProvider;
	
	public RessourceResultController(GuiCopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}

    @FXML //  fx:id="classesChart"
    private AreaChart<Number, Number> classesChart; // Value injected by FXMLLoader

    @FXML //  fx:id="cpuChart"
    private AreaChart<Number, Number> cpuChart; // Value injected by FXMLLoader

    @FXML //  fx:id="memoryChart"
    private AreaChart<Number, Number> memoryChart; // Value injected by FXMLLoader

    @FXML //  fx:id="threadChart"
    private AreaChart<Number, Number> threadChart; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert classesChart != null : "fx:id=\"classesChart\" was not injected: check your FXML file 'ResourceResult.fxml'.";
        assert cpuChart != null : "fx:id=\"cpuChart\" was not injected: check your FXML file 'ResourceResult.fxml'.";
        assert memoryChart != null : "fx:id=\"memoryChart\" was not injected: check your FXML file 'ResourceResult.fxml'.";
        assert threadChart != null : "fx:id=\"threadChart\" was not injected: check your FXML file 'ResourceResult.fxml'.";

        initChart();
    }

	private void initChart() {
		axisSystemCpuLoad = new XYChart.Series<Number, Number>();
		axisSystemCpuLoad.setName("SystemCpuLoad");
        cpuChart.getData().add(axisSystemCpuLoad);
        axisProcessCpuLoad = new XYChart.Series<Number, Number>();
        axisProcessCpuLoad.setName("ProcessCpuLoad");
        cpuChart.getData().add(axisProcessCpuLoad);
		cpuChart.getXAxis().setAnimated(false);
        
        axisThread = new XYChart.Series<Number, Number>();
        axisThread.setName("Threads count");
        threadChart.getData().add(axisThread);
        cpuChart.getXAxis().setAnimated(false);
        
        axisClasses = new XYChart.Series<Number, Number>();
        axisClasses.setName("Total loaded classes");
        classesChart.getData().add(axisClasses);
        
        axisMemory = new XYChart.Series<Number, Number>();
        axisMemory.setName("Memory usage");
        memoryChart.getData().add(axisMemory);
        
        axisFreeSystemMem= new XYChart.Series<Number, Number>();
        axisFreeSystemMem.setName("Free System Memory");
        memoryChart.getData().add(axisFreeSystemMem);
        
        cpuChart.getXAxis().setAnimated(false);
        threadChart.getXAxis().setAnimated(false);
        classesChart.getXAxis().setAnimated(false);
        memoryChart.getXAxis().setAnimated(false);
        
        
//        memoryChart.sett
	}
	
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("ResourceResult.fxml");
	}

	private static final int MAX_DATA_POINTS = 30;

	private XYChart.Series<Number, Number> axisSystemCpuLoad;
	private XYChart.Series<Number, Number> axisProcessCpuLoad;

	private XYChart.Series<Number, Number> axisFreeSystemMem;
	private XYChart.Series<Number, Number> axisThread;
	private XYChart.Series<Number, Number> axisClasses;
	private XYChart.Series<Number, Number> axisMemory;
	
	@Override
	public void showFilteredResult(List<SystemResourcesInfo> filteredlist, ResourceFilterModel usedFilter) {
		SystemResourcesInfo systemRessourcesInfo = filteredlist.get(0);
		Date date = systemRessourcesInfo.getTimestamp();

		updateChart(systemRessourcesInfo.getSystemCpuLoad(),date,axisSystemCpuLoad);
		updateChart(systemRessourcesInfo.getProcessCpuLoad(),date,axisProcessCpuLoad);
		
		updateChart(systemRessourcesInfo.getLiveThreadsCount(),date,axisThread);
		updateChart(systemRessourcesInfo.getTotalLoadedClassCount(),date,axisClasses);
		updateChart(systemRessourcesInfo.getHeapMemoryUsage()/1000000,date,axisMemory);
		updateChart(systemRessourcesInfo.getFreePhysicalMemorySize()/1000000,date,axisFreeSystemMem);
		
		ComponentUtil.setupXAxis((NumberAxis)cpuChart.getXAxis(),cpuChart.getData());
		ComponentUtil.setupXAxis((NumberAxis)threadChart.getXAxis(),threadChart.getData());
		ComponentUtil.setupXAxis((NumberAxis)classesChart.getXAxis(),classesChart.getData());
		ComponentUtil.setupXAxis((NumberAxis)memoryChart.getXAxis(),memoryChart.getData());
	}
	
	private void updateChart(Number value,Date date, XYChart.Series<Number, Number> series){
		ObservableList<Data<Number, Number>> data = series.getData();
		data.add(new XYChart.Data<Number, Number>(date.getTime(), value));
		if (data.size() > MAX_DATA_POINTS) {
			data.remove(0);
		}
	}

	@Override
	public List<SystemResourcesInfo> applyFilterInBackgroundThread(ResourceFilterModel filter) {
		return Arrays.asList(copperDataProvider.getSystemRessources());
	}

	@Override
	public boolean canLimitResult() {
		return false;
	}
	
	@Override
	public void clear() {
		cpuChart.getData().clear();
		axisThread.getData().clear();
		axisClasses.getData().clear();
		axisMemory.getData().clear();
		initChart();
	}
}
