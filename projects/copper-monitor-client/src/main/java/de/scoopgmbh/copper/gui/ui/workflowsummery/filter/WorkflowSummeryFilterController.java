package de.scoopgmbh.copper.gui.ui.workflowsummery.filter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterController;

public class WorkflowSummeryFilterController implements Initializable, FilterController<WorkflowSummeryFilterModel>, FxmlController {
	GuiCopperDataProvider copperDataProvider;


    @FXML //  fx:id="majorVersion"
    private TextField majorVersion; // Value injected by FXMLLoader

    @FXML //  fx:id="minorVersion"
    private TextField minorVersion; // Value injected by FXMLLoader

    @FXML //  fx:id="workflowClass"
    private TextField workflowClass; // Value injected by FXMLLoader


	private WorkflowSummeryFilterModel model;


	public WorkflowSummeryFilterController() {
		super();
	}

	@Override
	public void initialize(final URL url, final ResourceBundle rb) {
        assert majorVersion != null : "fx:id=\"majorVersion\" was not injected: check your FXML file 'WorkflowSummeryFilter.fxml'.";
        assert minorVersion != null : "fx:id=\"minorVersion\" was not injected: check your FXML file 'WorkflowSummeryFilter.fxml'.";
        assert workflowClass != null : "fx:id=\"workflowClass\" was not injected: check your FXML file 'WorkflowSummeryFilter.fxml'.";

        model = new WorkflowSummeryFilterModel();
        workflowClass.textProperty().bind(model.workflowclass);
        minorVersion.textProperty().bind(model.minorVersion);
        majorVersion.textProperty().bind(model.majorVersion);
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
