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
package de.scoopgmbh.copper.gui.ui.ressource.result;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterResultController;
import de.scoopgmbh.copper.gui.ui.ressource.filter.ResourceFilterModel;
import de.scoopgmbh.copper.monitor.adapter.model.SystemResourcesInfo;

public class RessourceResultController implements Initializable, FilterResultController<ResourceFilterModel,SystemResourcesInfo>, FxmlController {
	private final GuiCopperDataProvider copperDataProvider;
	
	public RessourceResultController(GuiCopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}

    @FXML //  fx:id="areaChart"
    private AreaChart<String, Number> areaChart; // Value injected by FXMLLoader

    @FXML //  fx:id="categoryAxis"
    private CategoryAxis categoryAxis; // Value injected by FXMLLoader

    @FXML //  fx:id="numberAxis"
    private NumberAxis numberAxis; // Value injected by FXMLLoader

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert areaChart != null : "fx:id=\"areaChart\" was not injected: check your FXML file 'EngineLoadResult.fxml'.";
        assert categoryAxis != null : "fx:id=\"categoryAxis\" was not injected: check your FXML file 'EngineLoadResult.fxml'.";
        assert numberAxis != null : "fx:id=\"numberAxis\" was not injected: check your FXML file 'EngineLoadResult.fxml'.";
        
        initChart();

    }

	private void initChart() {
        axis = new XYChart.Series<>();
        axis.setName("SystemCpuLoad");
        areaChart.getData().add(axis);
        
        axisProcessCpuLoad = new XYChart.Series<>();
        axisProcessCpuLoad.setName("ProcessCpuLoad");
        areaChart.getData().add(axisProcessCpuLoad);
	}
	
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("ResourceResult.fxml");
	}

	private static final int MAX_DATA_POINTS = 30;

	private XYChart.Series<String, Number> axis;
	private XYChart.Series<String, Number> axisProcessCpuLoad;
	
	@Override
	public void showFilteredResult(List<SystemResourcesInfo> filteredlist, ResourceFilterModel usedFilter) {

		SystemResourcesInfo systemRessourcesInfo = filteredlist.get(0);
		String date = new SimpleDateFormat("HH:mm:ss").format(systemRessourcesInfo.getTimestamp());

		ObservableList<Data<String, Number>> data = axis.getData();
		data.add(new XYChart.Data<String, Number>(date, systemRessourcesInfo.getSystemCpuLoad()));
		if (data.size() > MAX_DATA_POINTS) {
			data.remove(0);
		}
		
		data = axisProcessCpuLoad.getData();
		data.add(new XYChart.Data<String, Number>(date, systemRessourcesInfo.getProcessCpuLoad()));
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
		areaChart.getData().clear();
		initChart();
	}
}
