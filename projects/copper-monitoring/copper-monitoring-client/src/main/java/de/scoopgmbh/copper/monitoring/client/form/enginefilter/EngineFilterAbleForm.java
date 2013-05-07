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
package de.scoopgmbh.copper.monitoring.client.form.enginefilter;

import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.Form;
import de.scoopgmbh.copper.monitoring.client.form.ShowFormStrategy;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterAbleForm;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterController;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultController;
import de.scoopgmbh.copper.monitoring.client.util.MessageProvider;
import de.scoopgmbh.copper.monitoring.core.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitoring.core.model.ProcessingEngineInfo.EngineTyp;

public class EngineFilterAbleForm<F extends EngineFilterModel, R> extends FilterAbleForm<F, R> {

	private Node engineSelectionWidget;

	public EngineFilterAbleForm(final String titlePrefix,MessageProvider messageProvider, ShowFormStrategy<?> showFormStrategie,
			final Form<FilterController<F>> filterForm, Form<FilterResultController<F, R>> resultForm, GuiCopperDataProvider copperDataProvider) {
		super(messageProvider, showFormStrategie, filterForm, resultForm, copperDataProvider);
		

		List<ProcessingEngineInfo> engineList = copperDataProvider.getEngineList();
		engineSelectionWidget = new EngineSelectionWidget(filterForm.getController().getFilter().getEngineFilterModel(),engineList).createContent();

		dynamicTitleProperty().bindBidirectional(filterForm.getController().getFilter().getEngineFilterModel().selectedEngine, new StringConverter<ProcessingEngineInfo>(){
			@Override
			public ProcessingEngineInfo fromString(String string) {
				return null;
			}

			@Override
			public String toString(ProcessingEngineInfo object) {
				if (object==null){
					return "";
				}
				return titlePrefix+": "+object.getId()+"("+(object.getTyp()==EngineTyp.PERSISTENT?"P":"T")+")";
			}
		});
	}

	@Override
	protected Node createLeftFilterPart(){
		HBox pane = new HBox();
		HBox.setMargin(engineSelectionWidget, new Insets(0, 0, 0, 3));
		pane.getChildren().add(engineSelectionWidget);
		pane.getChildren().add(new Separator(Orientation.VERTICAL));
		return pane;
	}
	
	@Override
	public void setDynamicTitle(String staticTitle) {
		//do nothing
	}

}
