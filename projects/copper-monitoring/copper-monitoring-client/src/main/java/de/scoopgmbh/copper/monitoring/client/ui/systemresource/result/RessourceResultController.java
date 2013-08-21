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
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
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
		seriesSystemCpuLoad = new XYChart.Series<Number, Number>();
		seriesSystemCpuLoad.setName("SystemCpuLoad");
        cpuChart.getData().add(seriesSystemCpuLoad);
        seriesProcessCpuLoad = new XYChart.Series<Number, Number>();
        seriesProcessCpuLoad.setName("ProcessCpuLoad");
        cpuChart.getData().add(seriesProcessCpuLoad);
		cpuChart.getXAxis().setAnimated(false);
        
        seriesThread = new XYChart.Series<Number, Number>();
        seriesThread.setName("Threads count");
        threadChart.getData().add(seriesThread);
        cpuChart.getXAxis().setAnimated(false);
        
        seriesClasses = new XYChart.Series<Number, Number>();
        seriesClasses.setName("Total loaded classes");
        classesChart.getData().add(seriesClasses);
        
        seriesMemory = new XYChart.Series<Number, Number>();
        seriesMemory.setName("Memory usage");
        memoryChart.getData().add(seriesMemory);
        
        seriesFreeSystemMem= new XYChart.Series<Number, Number>();
        seriesFreeSystemMem.setName("Free System Memory");
        memoryChart.getData().add(seriesFreeSystemMem);
        
        cpuChart.getXAxis().setAnimated(false);
        threadChart.getXAxis().setAnimated(false);
        classesChart.getXAxis().setAnimated(false);
        memoryChart.getXAxis().setAnimated(false);
        
        
//        memoryChart.sett
	}
	
	@Override
	public URL getFxmlResource() {
		return getClass().getResource("ResourceResult.fxml");
	}

	private XYChart.Series<Number, Number> seriesSystemCpuLoad;
	private XYChart.Series<Number, Number> seriesProcessCpuLoad;

	private XYChart.Series<Number, Number> seriesFreeSystemMem;
	private XYChart.Series<Number, Number> seriesThread;
	private XYChart.Series<Number, Number> seriesClasses;
	private XYChart.Series<Number, Number> seriesMemory;
	
	@Override
	public void showFilteredResult(List<SystemResourcesInfo> filteredlist, ResourceFilterModel usedFilter) {
		clear();
		for (SystemResourcesInfo systemRessourcesInfo: filteredlist){
			Date date = systemRessourcesInfo.getTimeStamp();
			updateChart(systemRessourcesInfo.getSystemCpuLoad(),date,seriesSystemCpuLoad);
			updateChart(systemRessourcesInfo.getProcessCpuLoad(),date,seriesProcessCpuLoad);
			
			updateChart(systemRessourcesInfo.getLiveThreadsCount(),date,seriesThread);
			updateChart(systemRessourcesInfo.getTotalLoadedClassCount(),date,seriesClasses);
			updateChart(systemRessourcesInfo.getHeapMemoryUsage()/1000000,date,seriesMemory);
			updateChart(systemRessourcesInfo.getFreePhysicalMemorySize()/1000000,date,seriesFreeSystemMem);
			
		}
		ComponentUtil.setupXAxis((NumberAxis)cpuChart.getXAxis(),cpuChart.getData());
		ComponentUtil.setupXAxis((NumberAxis)threadChart.getXAxis(),threadChart.getData());
		ComponentUtil.setupXAxis((NumberAxis)classesChart.getXAxis(),classesChart.getData());
		ComponentUtil.setupXAxis((NumberAxis)memoryChart.getXAxis(),memoryChart.getData());
	}
	
	private void updateChart(Number value,Date date, XYChart.Series<Number, Number> series){
		series.getData().add(new XYChart.Data<Number, Number>(date.getTime(), value));
	}

	@Override
	public List<SystemResourcesInfo> applyFilterInBackgroundThread(ResourceFilterModel filter) {
		return copperDataProvider.getSystemRessources(filter.fromToFilterModel.from.get(),filter.fromToFilterModel.to.get(),filter.maxCountFilterModel.getMaxCount());
	}
	
	@Override
	public void clear() {
        cpuChart.setAnimated(false);
        threadChart.setAnimated(false);
        classesChart.setAnimated(false);
        memoryChart.setAnimated(false);
        
		seriesSystemCpuLoad.getData().clear();
		seriesProcessCpuLoad.getData().clear();
		seriesThread.getData().clear();
		seriesClasses.getData().clear();
		seriesMemory.getData().clear();
		seriesFreeSystemMem.getData().clear();
		
        cpuChart.getXAxis().setAnimated(true);
        threadChart.getXAxis().setAnimated(true);
        classesChart.getXAxis().setAnimated(true);
        memoryChart.getXAxis().setAnimated(true);
	}
}
