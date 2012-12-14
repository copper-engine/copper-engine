package de.scoopgmbh.copper.gui.ui.workflowinstance.filter;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterController;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;

public class WorkflowInstanceFilterController implements Initializable, FilterController<WorkflowInstanceFilterModel>, FxmlController {
	WorkflowInstanceFilterModel model;

	@FXML
	// fx:id="priorityField"
	private TextField priorityField; // Value injected by FXMLLoader

	@FXML
	// fx:id="stateChoice"
	private ChoiceBox<WorkflowInstanceState> stateChoice; // Value injected by FXMLLoader

	@Override
	// This method is called by the FXMLLoader when initialization is complete
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		assert priorityField != null : "fx:id=\"priorityField\" was not injected: check your FXML file 'WorkflowInstanceFilter.fxml'.";
		assert stateChoice != null : "fx:id=\"stateChoice\" was not injected: check your FXML file 'WorkflowInstanceFilter.fxml'.";

        model = new WorkflowInstanceFilterModel();
        priorityField.textProperty().bind(model.priority.asString());
        
        stateChoice.setItems(FXCollections.observableList(Arrays.asList(WorkflowInstanceState.values())));
        stateChoice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<WorkflowInstanceState>() {
            public void changed(ObservableValue<? extends WorkflowInstanceState> observableValue, WorkflowInstanceState anEnum, WorkflowInstanceState anEnum1) {
            	model.state.setValue(anEnum1);
            }
        });

	}

	@Override
	public WorkflowInstanceFilterModel getFilter() {
		return model;
	}

	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("WorkflowInstanceFilter.fxml");
	}
	
	
}
