package de.scoopgmbh.copper.gui.ui.worklowinstancedetail.filter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterController;

public class WorkflowInstanceDetailFilterController implements Initializable, FilterController<WorkflowInstanceDetailFilterModel>, FxmlController {
	private WorkflowInstanceDetailFilterModel model;

	public WorkflowInstanceDetailFilterController(String workflowInstanceId) {
		super();
		model = new WorkflowInstanceDetailFilterModel();
		model.workflowInstanceId.setValue(workflowInstanceId);
	}
	
	
	public void setFilter(String workflowInstanceId){
		model.workflowInstanceId.setValue(workflowInstanceId);
	}

    @FXML //  fx:id="workflowInstanceIdTextfield"
    private TextField workflowInstanceIdTextfield; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert workflowInstanceIdTextfield != null : "fx:id=\"workflowInstanceIdTextfield\" was not injected: check your FXML file 'WorkflowInstanceDetailFilter.fxml'.";

        workflowInstanceIdTextfield.textProperty().bindBidirectional(model.workflowInstanceId);
	}

	@Override
	public WorkflowInstanceDetailFilterModel getFilter() {
		return model;
	}

	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("WorkflowInstanceDetailFilter.fxml");
	}
	
	
}
