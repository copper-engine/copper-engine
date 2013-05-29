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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
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

	public EngineFilterAbleForm(MessageProvider messageProvider, ShowFormStrategy<?> showFormStrategie,
			final Form<FilterController<F>> filterForm, Form<FilterResultController<F, R>> resultForm, GuiCopperDataProvider copperDataProvider) {
		super(messageProvider, showFormStrategie, filterForm, resultForm, copperDataProvider);
		

		List<ProcessingEngineInfo> engineList = copperDataProvider.getEngineList();
		engineSelectionWidget = new EngineSelectionWidget(filterForm.getController().getFilter().getEngineFilterModel(),engineList).createContent();

		createTitle(filterForm.getController().getFilter().getEngineFilterModel().selectedEngine.get());
		filterForm.getController().getFilter().getEngineFilterModel().selectedEngine.addListener(new ChangeListener<ProcessingEngineInfo>() {
			@Override
			public void changed(ObservableValue<? extends ProcessingEngineInfo> observable, ProcessingEngineInfo oldValue, ProcessingEngineInfo newValue) {
				if (newValue!=null){
					createTitle(newValue);
				}
			}
		});
		staticTitleProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue!=null){
					createTitle(filterForm.getController().getFilter().getEngineFilterModel().selectedEngine.get());
				}
			}
		});
	}
	
	private void createTitle(ProcessingEngineInfo engine) {
		displayedTitleProperty().set(staticTitleProperty().get()+": "+engine.getId()+"("+(engine.getTyp()==EngineTyp.PERSISTENT?"P":"T")+")");
	}

	@Override
	protected Node createLeftFilterPart(){
		MenuButton engineButtton = new MenuButton("Engine");
		engineButtton.setPrefWidth(20);
		CustomMenuItem customMenuItem = new CustomMenuItem();
		engineButtton.getItems().add(customMenuItem);
		customMenuItem.getStyleClass().setAll("noSelectAnimationMenueItem","menu-item");
		HBox hbox = new HBox(3);
		hbox.setAlignment(Pos.CENTER_LEFT);
		HBox.setMargin(engineSelectionWidget, new Insets(0, 0, 0, 3));
		hbox.getChildren().add(engineSelectionWidget);
		customMenuItem.setContent(hbox);
		
		HBox pane = new HBox();
		pane.setAlignment(Pos.CENTER_LEFT);
		engineButtton.setPrefWidth(100);
		pane.getChildren().add(engineButtton);
		pane.getChildren().add(new Separator(Orientation.VERTICAL));
		return pane;
	}

}
