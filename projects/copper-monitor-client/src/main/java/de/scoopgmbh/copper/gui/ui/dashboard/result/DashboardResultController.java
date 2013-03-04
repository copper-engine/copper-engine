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
package de.scoopgmbh.copper.gui.ui.dashboard.result;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.context.FormContext;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.EmptyFilterModel;
import de.scoopgmbh.copper.gui.form.filter.FilterResultController;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowStateSummary;

public class DashboardResultController implements Initializable, FilterResultController<EmptyFilterModel,DashboardResultModel>, FxmlController {
	private final GuiCopperDataProvider copperDataProvider;
	private final FormContext formContext;
	
	public DashboardResultController(GuiCopperDataProvider copperDataProvider, FormContext formContext) {
		super();
		this.copperDataProvider = copperDataProvider;
		this.formContext = formContext;
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
			formContext.createEngineForm(engines,processingEngineInfo,dashboardResultModel).show();
		}
	}

	@Override
	public List<DashboardResultModel> applyFilterInBackgroundThread(EmptyFilterModel filter) {
		List<ProcessingEngineInfo> engines = copperDataProvider.getEngineList();
		Map<String, WorkflowStateSummary> engineIdTostateSummery = new HashMap<>();
		for (ProcessingEngineInfo processingEngineInfo: engines){
			engineIdTostateSummery.put(processingEngineInfo.getId(), copperDataProvider.getCopperLoadInfo(processingEngineInfo));
		}
		return Arrays.asList(new DashboardResultModel(engineIdTostateSummery,engines));
	}

	@Override
	public boolean canLimitResult() {
		return false;
	}
	
	@Override
	public void clear() {
	}
}
