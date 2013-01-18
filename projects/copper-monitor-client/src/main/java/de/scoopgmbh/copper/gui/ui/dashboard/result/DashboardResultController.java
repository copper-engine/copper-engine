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
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.EmptyFilterModel;
import de.scoopgmbh.copper.gui.form.filter.FilterResultController;

public class DashboardResultController implements Initializable, FilterResultController<EmptyFilterModel,DashboardResultModel>, FxmlController {
	private final GuiCopperDataProvider copperDataProvider;
	
	public DashboardResultController(GuiCopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}



    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {

        

    }

	
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("DashboardResult.fxml");
	}

	@Override
	public void showFilteredResult(List<DashboardResultModel> filteredlist, EmptyFilterModel usedFilter) {
		

		
	}

	@Override
	public List<DashboardResultModel> applyFilterInBackgroundThread(EmptyFilterModel filter) {
		return null;//Arrays.asList(new DashboardResultModel(copperDataProvider.getCopperLoadInfo()));
	}

	@Override
	public boolean canLimitResult() {
		return false;
	}
	
	@Override
	public void clear() {
	}
}
