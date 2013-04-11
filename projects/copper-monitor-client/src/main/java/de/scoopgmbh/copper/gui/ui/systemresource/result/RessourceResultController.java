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
package de.scoopgmbh.copper.gui.ui.systemresource.result;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterResultController;
import de.scoopgmbh.copper.gui.ui.systemresource.filter.ResourceFilterModel;
import de.scoopgmbh.copper.monitor.adapter.model.SystemResourcesInfo;

public class RessourceResultController implements Initializable, FilterResultController<ResourceFilterModel,SystemResourcesInfo>, FxmlController {
	private final GuiCopperDataProvider copperDataProvider;
	
	public RessourceResultController(GuiCopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}

    @FXML //  fx:id="classesChart"
    private AreaChart<String, Number> classesChart; // Value injected by FXMLLoader

    @FXML //  fx:id="cpuChart"
    private AreaChart<String, Number> cpuChart; // Value injected by FXMLLoader

    @FXML //  fx:id="memoryChart"
    private AreaChart<String, Number> memoryChart; // Value injected by FXMLLoader

    @FXML //  fx:id="threadChart"
    private AreaChart<String, Number> threadChart; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert classesChart != null : "fx:id=\"classesChart\" was not injected: check your FXML file 'ResourceResult.fxml'.";
        assert cpuChart != null : "fx:id=\"cpuChart\" was not injected: check your FXML file 'ResourceResult.fxml'.";
        assert memoryChart != null : "fx:id=\"memoryChart\" was not injected: check your FXML file 'ResourceResult.fxml'.";
        assert threadChart != null : "fx:id=\"threadChart\" was not injected: check your FXML file 'ResourceResult.fxml'.";

        
        initChart();

    }

	private void initChart() {
		axisSystemCpuLoad = new XYChart.Series<String, Number>();
		axisSystemCpuLoad.setName("SystemCpuLoad");
        cpuChart.getData().add(axisSystemCpuLoad);
        axisProcessCpuLoad = new XYChart.Series<String, Number>();
        axisProcessCpuLoad.setName("ProcessCpuLoad");
        cpuChart.getData().add(axisProcessCpuLoad);
        
        axisThread = new XYChart.Series<String, Number>();
        axisThread.setName("Threads count");
        threadChart.getData().add(axisThread);
        
        axisClasses = new XYChart.Series<String, Number>();
        axisClasses.setName("Total loaded classes");
        classesChart.getData().add(axisClasses);
        
        axisMemory = new XYChart.Series<String, Number>();
        axisMemory.setName("Heap memory usage");
        memoryChart.getData().add(axisMemory);
	}
	
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("ResourceResult.fxml");
	}

	private static final int MAX_DATA_POINTS = 30;

	private XYChart.Series<String, Number> axisSystemCpuLoad;
	private XYChart.Series<String, Number> axisProcessCpuLoad;

	private XYChart.Series<String, Number> axisThread;
	private XYChart.Series<String, Number> axisClasses;
	private XYChart.Series<String, Number> axisMemory;
	
	@Override
	public void showFilteredResult(List<SystemResourcesInfo> filteredlist, ResourceFilterModel usedFilter) {
		
		SystemResourcesInfo systemRessourcesInfo = filteredlist.get(0);
		String date = new SimpleDateFormat("HH:mm:ss").format(systemRessourcesInfo.getTimestamp());

		updateChart(systemRessourcesInfo.getSystemCpuLoad(),date,axisSystemCpuLoad);
		updateChart(systemRessourcesInfo.getProcessCpuLoad(),date,axisProcessCpuLoad);
		
		updateChart(systemRessourcesInfo.getLiveThreadsCount(),date,axisThread);
		updateChart(systemRessourcesInfo.getTotalLoadedClassCount(),date,axisClasses);
		updateChart(systemRessourcesInfo.getHeapMemoryUsage(),date,axisMemory);
	}
	
	private void updateChart(Number value,String date, XYChart.Series<String, Number> axis){
		ObservableList<Data<String, Number>> data = axis.getData();
		data.add(new XYChart.Data<String, Number>(date, value));
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
