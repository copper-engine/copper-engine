package de.scoopgmbh.copper.gui.ui.workflowsummery.filter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.TextField;
import de.scoopgmbh.copper.gui.context.FormContext;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterController;

public class WorkflowSummeryFilterController implements Initializable, FilterController<WorkflowSummeryFilterModel>, FxmlController {
	private WorkflowSummeryFilterModel model;
	private final FormContext formFactory;

	public WorkflowSummeryFilterController(FormContext formFactory) {
		super();
		this.formFactory = formFactory;
	}
	
	
	public void setFilter(String workflowclass, String minorVersion, String majorVersion){
		model.workflowclass.setValue(workflowclass);
		model.majorVersion.setValue(minorVersion);
		model.minorVersion.setValue(majorVersion);
	}
	
    @FXML //  fx:id="majorVersion"
    private TextField majorVersion; // Value injected by FXMLLoader

    @FXML //  fx:id="minorVersion"
    private TextField minorVersion; // Value injected by FXMLLoader

    @FXML //  fx:id="workflowClass"
    private TextField workflowClass; // Value injected by FXMLLoader

    @FXML //  fx:id="searchMenueItem"
    private CustomMenuItem searchMenueItem; // Value injected by FXMLLoader

	@Override
	public void initialize(final URL url, final ResourceBundle rb) {
        assert majorVersion != null : "fx:id=\"majorVersion\" was not injected: check your FXML file 'WorkflowSummeryFilter.fxml'.";
        assert minorVersion != null : "fx:id=\"minorVersion\" was not injected: check your FXML file 'WorkflowSummeryFilter.fxml'.";
        assert workflowClass != null : "fx:id=\"workflowClass\" was not injected: check your FXML file 'WorkflowSummeryFilter.fxml'.";
        assert searchMenueItem != null : "fx:id=\"searchMenueItem\" was not injected: check your FXML file 'WorkflowSummeryFilter.fxml'.";
        
        model = new WorkflowSummeryFilterModel();
        workflowClass.textProperty().bindBidirectional(model.workflowclass);
        minorVersion.textProperty().bindBidirectional(model.minorVersion);
        majorVersion.textProperty().bindBidirectional(model.majorVersion);
        
        searchMenueItem.setContent(formFactory.createWorkflowClassesTreeForm(this).createContent());
        
        searchMenueItem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				
			}
		});
//        searchMenueItem.getParentMenu().
	}

	@Override
	public WorkflowSummeryFilterModel getFilter() {
		return model;
	}

	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("WorkflowSummeryFilter.fxml");
	}
	
	
}
