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
package de.scoopgmbh.copper.monitoring.client.ui.dashboard.result;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;
import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.filter.EmptyFilterModel;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultControllerBase;
import de.scoopgmbh.copper.monitoring.core.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowStateSummary;

public class DashboardResultController extends FilterResultControllerBase<EmptyFilterModel,DashboardResultModel> implements Initializable{
	private final GuiCopperDataProvider copperDataProvider;
	private final DashboardDependencyFactory dashboardPartsFactory;
	
	public DashboardResultController(GuiCopperDataProvider copperDataProvider, DashboardDependencyFactory dashboardPartsFactory) {
		super();
		this.copperDataProvider = copperDataProvider;
		this.dashboardPartsFactory = dashboardPartsFactory;
	}

    @FXML //  fx:id="engines"
    private TabPane engines; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert engines != null : "fx:id=\"engines\" was not injected: check your FXML file 'DashboardResult.fxml'.";
    }
	
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("DashboardResult.fxml");
	}

	@Override
	public void showFilteredResult(List<DashboardResultModel> filteredlist, EmptyFilterModel usedFilter) {
		DashboardResultModel dashboardResultModel = filteredlist.get(0);
		engines.getTabs().clear();
		for (ProcessingEngineInfo processingEngineInfo: dashboardResultModel.engines){
			dashboardPartsFactory.createEngineForm(engines,processingEngineInfo,dashboardResultModel).show();
		}
	}

	@Override
	public List<DashboardResultModel> applyFilterInBackgroundThread(EmptyFilterModel filter) {
		List<ProcessingEngineInfo> engines = copperDataProvider.getEngineList();
		Map<String, WorkflowStateSummary> engineIdTostateSummery = new HashMap<String, WorkflowStateSummary>();
		for (ProcessingEngineInfo processingEngineInfo: engines){
			engineIdTostateSummery.put(processingEngineInfo.getId(), copperDataProvider.getCopperLoadInfo(processingEngineInfo));
		}
		return Arrays.asList(new DashboardResultModel(engineIdTostateSummery,engines));
	}
	
	@Override
	public void clear() {
	}
}
