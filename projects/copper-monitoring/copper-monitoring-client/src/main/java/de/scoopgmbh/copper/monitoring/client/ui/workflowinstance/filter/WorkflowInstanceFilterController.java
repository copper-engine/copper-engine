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
package de.scoopgmbh.copper.monitoring.client.ui.workflowinstance.filter;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javafx.util.converter.DateStringConverter;
import de.scoopgmbh.copper.monitoring.client.form.FxmlController;
import de.scoopgmbh.copper.monitoring.client.form.filter.FilterController;
import de.scoopgmbh.copper.monitoring.client.form.filter.defaultfilter.DefaultFilterFactory;
import de.scoopgmbh.copper.monitoring.client.form.filter.enginefilter.BaseEngineFilterController;
import de.scoopgmbh.copper.monitoring.client.util.DateValidationHelper;
import de.scoopgmbh.copper.monitoring.core.model.ProcessingEngineInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceState;

public class WorkflowInstanceFilterController extends BaseEngineFilterController<WorkflowInstanceFilterModel> implements Initializable, FxmlController {
	public WorkflowInstanceFilterController(List<ProcessingEngineInfo> availableEngines) {
		super(availableEngines,new WorkflowInstanceFilterModel());
	}

	static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";

	public class EmptySelectionWorkaround{
		public WorkflowInstanceState value;
		public String text;
		public EmptySelectionWorkaround(WorkflowInstanceState value, String text) {
			super();
			this.value = value;
			this.text = text;
		}
		
	}

    @FXML //  fx:id="priorityField"
    private TextField priorityField; // Value injected by FXMLLoader

    @FXML //  fx:id="stateChoice"
    private ChoiceBox<EmptySelectionWorkaround> stateChoice; // Value injected by FXMLLoader

    @FXML //  fx:id="workflowClass"
    private TextField workflowClass; // Value injected by FXMLLoader

    @FXML //  fx:id="from"
    private TextField from; // Value injected by FXMLLoader

    @FXML //  fx:id="to"
    private TextField to; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert from != null : "fx:id=\"from\" was not injected: check your FXML file 'WorkflowInstanceFilter.fxml'.";
        assert priorityField != null : "fx:id=\"priorityField\" was not injected: check your FXML file 'WorkflowInstanceFilter.fxml'.";
        assert stateChoice != null : "fx:id=\"stateChoice\" was not injected: check your FXML file 'WorkflowInstanceFilter.fxml'.";
        assert to != null : "fx:id=\"to\" was not injected: check your FXML file 'WorkflowInstanceFilter.fxml'.";
        assert workflowClass != null : "fx:id=\"workflowClass\" was not injected: check your FXML file 'WorkflowInstanceFilter.fxml'.";

        
        priorityField.textProperty().bindBidirectional(model.priority);
        workflowClass.textProperty().bindBidirectional(model.version.classname);
        
        from.textProperty().bindBidirectional(model.from, new DateStringConverter(DATE_TIME_FORMAT));
        to.textProperty().bindBidirectional(model.to, new DateStringConverter(DATE_TIME_FORMAT));
        DateValidationHelper.addValidation(from,DATE_TIME_FORMAT);
        DateValidationHelper.addValidation(to,DATE_TIME_FORMAT);
        ArrayList<EmptySelectionWorkaround> states = new ArrayList<EmptySelectionWorkaround>();
        for (WorkflowInstanceState state: WorkflowInstanceState.values()){
        	states.add(new EmptySelectionWorkaround(state,state.toString()));
    	}	
        EmptySelectionWorkaround emptyItem = new EmptySelectionWorkaround(null,"any");
		states.add(emptyItem);
        stateChoice.setItems(FXCollections.observableList(states));
        stateChoice.setConverter(new StringConverter<WorkflowInstanceFilterController.EmptySelectionWorkaround>() {
			@Override
			public String toString(EmptySelectionWorkaround object) {
				return object.text;
			}
			
			@Override
			public EmptySelectionWorkaround fromString(String string) {
				return null;
			}
		});
        stateChoice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<EmptySelectionWorkaround>() {
            @Override
			public void changed(ObservableValue<? extends EmptySelectionWorkaround> observableValue, EmptySelectionWorkaround anEnum, EmptySelectionWorkaround anEnum1) {
            	model.state.setValue(anEnum1.value);
            }
        });
        stateChoice.getSelectionModel().select(emptyItem);

	}
    
	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("WorkflowInstanceFilter.fxml");
	}
	
	@Override
	public boolean supportsFiltering() {
		return true;
	}
	
	@Override
	public long getDefaultRefreshIntervall() {
		return FilterController.DEFAULT_REFRESH_INTERVALL;
	}

	@Override
	public Node createAdditionalFilter() {
		return new DefaultFilterFactory().createMaxCount(model.maxCountFilterModel);
	}
	
}
