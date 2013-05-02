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
package de.scoopgmbh.copper.monitoring.client.ui.load.result;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultControllerBase;
import de.scoopgmbh.copper.monitoring.client.ui.load.filter.EngineLoadFilterModel;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowStateSummary;

public class EngineLoadResultController extends FilterResultControllerBase<EngineLoadFilterModel,WorkflowStateSummary> implements Initializable {
	private final GuiCopperDataProvider copperDataProvider;
	
	public EngineLoadResultController(GuiCopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}

    @FXML //  fx:id="areaChart"
    private AreaChart<String, Number> areaChart; // Value injected by FXMLLoader

    @FXML //  fx:id="categoryAxis"
    private CategoryAxis categoryAxis; // Value injected by FXMLLoader

    @FXML //  fx:id="numberAxis"
    private NumberAxis numberAxis; // Value injected by FXMLLoader

    private Map<WorkflowInstanceState,XYChart.Series<String, Number>> stateToAxis = new HashMap<WorkflowInstanceState,XYChart.Series<String, Number>>();

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert areaChart != null : "fx:id=\"areaChart\" was not injected: check your FXML file 'EngineLoadResult.fxml'.";
        assert categoryAxis != null : "fx:id=\"categoryAxis\" was not injected: check your FXML file 'EngineLoadResult.fxml'.";
        assert numberAxis != null : "fx:id=\"numberAxis\" was not injected: check your FXML file 'EngineLoadResult.fxml'.";
        
        initChart();

    }

	private void initChart() {
		stateToAxis.clear();
        for (WorkflowInstanceState workflowInstanceState: WorkflowInstanceState.values()){
        	XYChart.Series<String, Number> axis= new XYChart.Series<String, Number>();
        	axis.setName(workflowInstanceState.toString());
        	stateToAxis.put(workflowInstanceState,axis);
        	areaChart.getData().add(axis);
        }
//        areaChart.getXAxis().setAnimated(false);
	}
	
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("EngineLoadResult.fxml");
	}

	private static final int MAX_DATA_POINTS = 30;
	@Override
	public void showFilteredResult(List<WorkflowStateSummary> filteredlist, EngineLoadFilterModel usedFilter) {
		
		WorkflowStateSummary copperLoadInfo = filteredlist.get(0);	
		String date = new SimpleDateFormat("HH:mm:ss").format(new Date());
		
		for (Entry<WorkflowInstanceState,Integer> entry: copperLoadInfo.getNumberOfWorkflowInstancesWithState().entrySet()){
			Series<String, Number> axis = stateToAxis.get(entry.getKey());
			ObservableList<Data<String, Number>> data = axis.getData();
			data.add(new XYChart.Data<String, Number>(date, entry.getValue()));
			if (data.size() > MAX_DATA_POINTS) {
				data.remove(0);
			}
			
			if (usedFilter.stateFilters.get(entry.getKey()).getValue()){
				if (!areaChart.getData().contains(axis)){
					areaChart.getData().add(axis);
				}
			} else {
				areaChart.getData().remove(axis);
			}
		}
		
	}

	@Override
	public List<WorkflowStateSummary> applyFilterInBackgroundThread(EngineLoadFilterModel filter) {
		return Arrays.asList(copperDataProvider.getCopperLoadInfo(filter.enginePoolModel.selectedEngine.getValue()));
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
