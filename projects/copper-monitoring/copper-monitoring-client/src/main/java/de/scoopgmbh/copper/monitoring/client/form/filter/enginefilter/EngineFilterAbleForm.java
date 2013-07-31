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
package de.scoopgmbh.copper.monitoring.client.form.filter.enginefilter;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import de.scoopgmbh.copper.monitoring.client.form.Form;
import de.scoopgmbh.copper.monitoring.client.form.ShowFormStrategy;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterAbleForm;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterController;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterResultController;
import de.scoopgmbh.copper.monitoring.client.form.issuereporting.IssueReporter;
import de.scoopgmbh.copper.monitoring.client.util.MessageProvider;
import de.scoopgmbh.copper.monitoring.core.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitoring.core.model.ProcessingEngineInfo.EngineTyp;

public class EngineFilterAbleForm<F extends EnginePoolFilterModel, R> extends FilterAbleForm<F, R> {


	public EngineFilterAbleForm(MessageProvider messageProvider, ShowFormStrategy<?> showFormStrategie,
			final Form<FilterController<F>> filterForm, Form<FilterResultController<F, R>> resultForm, IssueReporter exceptionHandler) {
		super(messageProvider, showFormStrategie, filterForm, resultForm, exceptionHandler);
		
		filterForm.getController().getFilter().selectedEngine.addListener(new ChangeListener<ProcessingEngineInfo>() {
			@Override
			public void changed(ObservableValue<? extends ProcessingEngineInfo> observable, ProcessingEngineInfo oldValue,
					ProcessingEngineInfo newValue) {
				if (newValue!=null){
					createTitle(newValue);
				}
			}
		});
		
		
		filterForm.getController().getFilter().selectedEngine.addListener(new ChangeListener<ProcessingEngineInfo>() {
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
					createTitle(filterForm.getController().getFilter().selectedEngine.get());
				}
			}
		});
	}
	
	private void createTitle(ProcessingEngineInfo engine) {
		displayedTitleProperty().set(staticTitleProperty().get()+": "+engine.getId()+"("+(engine.getTyp()==EngineTyp.PERSISTENT?"P":"T")+")");
	}

}
