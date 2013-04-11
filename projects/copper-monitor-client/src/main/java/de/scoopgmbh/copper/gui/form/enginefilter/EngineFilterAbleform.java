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
package de.scoopgmbh.copper.gui.form.enginefilter;

import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.Form;
import de.scoopgmbh.copper.gui.form.ShowFormStrategy;
import de.scoopgmbh.copper.gui.form.filter.FilterAbleForm;
import de.scoopgmbh.copper.gui.form.filter.FilterController;
import de.scoopgmbh.copper.gui.form.filter.FilterResultController;
import de.scoopgmbh.copper.gui.util.EngineFilter;
import de.scoopgmbh.copper.gui.util.MessageProvider;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitor.adapter.model.ProcessingEngineInfo.EngineTyp;

public class EngineFilterAbleform<F extends EngineFilter, R> extends FilterAbleForm<F, R> {

	private ChoiceBox<ProcessingEngineInfo> choicebox;

	public EngineFilterAbleform(final String titlePrefix,MessageProvider messageProvider, ShowFormStrategy<?> showFormStrategie,
			final Form<FilterController<F>> filterForm, Form<FilterResultController<F, R>> resultForm, GuiCopperDataProvider copperDataProvider) {
		super(messageProvider, showFormStrategie, filterForm, resultForm, copperDataProvider);
		// TODO Auto-generated constructor stub
		
		choicebox = new ChoiceBox<ProcessingEngineInfo>();
		choicebox.setConverter(new StringConverter<ProcessingEngineInfo>() {
			@Override
			public String toString(ProcessingEngineInfo object) {
				return object.getId()+"("+object.getTyp()+")";
			}
			
			@Override
			public ProcessingEngineInfo fromString(String string) {
				return null;
			}
		});
		
		
//		filterForm.getController().getFilter().engine
//		
//		
//		bind(choicebox.getSelectionModel().selectedItemProperty());
		
		choicebox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ProcessingEngineInfo>() {
			@Override
			public void changed(ObservableValue<? extends ProcessingEngineInfo> observable, ProcessingEngineInfo oldValue,
					ProcessingEngineInfo newValue) {
				filterForm.getController().getFilter().engine.setValue(newValue);
			}
		});
		
		
        filterForm.getController().getFilter().engine.addListener(new ChangeListener<ProcessingEngineInfo>() {
			@Override
			public void changed(ObservableValue<? extends ProcessingEngineInfo> observable, ProcessingEngineInfo oldValue,
					ProcessingEngineInfo newValue) {
				for (ProcessingEngineInfo processingEngineInfo: choicebox.getItems()){
					if (processingEngineInfo.getId()!=null && processingEngineInfo.getId().equals(newValue.getId())){
						choicebox.getSelectionModel().select(processingEngineInfo);
					}
				}
			}
		});
		
		List<ProcessingEngineInfo> engineList = copperDataProvider.getEngineList();
		for (ProcessingEngineInfo engineFilter: engineList){
			choicebox.getItems().add(engineFilter);
		}
		choicebox.getSelectionModel().selectFirst();
		

		dynamicTitleProperty().bindBidirectional(filterForm.getController().getFilter().engine, new StringConverter<ProcessingEngineInfo>(){
			@Override
			public ProcessingEngineInfo fromString(String string) {
				return null;
			}

			@Override
			public String toString(ProcessingEngineInfo object) {
				return titlePrefix+": "+object.getId()+"("+(object.getTyp()==EngineTyp.PERSISTENT?"P":"T")+")";
			}
		});
	}

	@Override
	protected void beforFilterHook(HBox filterbox){
		HBox.setMargin(choicebox, new Insets(0, 0, 0, 5));
		filterbox.getChildren().add(choicebox);
		filterbox.getChildren().add(new Separator(Orientation.VERTICAL));
	}
	
	@Override
	public void setDynamicTitle(String staticTitle) {
		//do nothing
	}

}
