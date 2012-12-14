package de.scoopgmbh.copper.gui.ui.audittrail.filter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterController;

public class AuditTrailFilterController implements Initializable, FilterController<AuditTrailFilterModel>, FxmlController {

	private AuditTrailFilterModel model;
	public AuditTrailFilterController() {
		super();
	}

    @FXML //  fx:id="correlationId"
    private TextField correlationId; // Value injected by FXMLLoader

    @FXML //  fx:id="level"
    private TextField level; // Value injected by FXMLLoader

    @FXML //  fx:id="workflowClass"
    private TextField workflowClass; // Value injected by FXMLLoader

    @FXML //  fx:id="workflowInstanceId"
    private TextField workflowInstanceId; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert correlationId != null : "fx:id=\"correlationId\" was not injected: check your FXML file 'AuditTrailFilter.fxml'.";
        assert level != null : "fx:id=\"level\" was not injected: check your FXML file 'AuditTrailFilter.fxml'.";
        assert workflowClass != null : "fx:id=\"workflowClass\" was not injected: check your FXML file 'AuditTrailFilter.fxml'.";
        assert workflowInstanceId != null : "fx:id=\"workflowInstanceId\" was not injected: check your FXML file 'AuditTrailFilter.fxml'.";


        model = new AuditTrailFilterModel();
        workflowClass.textProperty().bind(model.workflowClass);
        level.textProperty().bind(model.level.asString());
        correlationId.textProperty().bind(model.correlationId);
        workflowInstanceId.textProperty().bind(model.workflowInstanceId);
	}

	@Override
	public AuditTrailFilterModel getFilter() {
		return model;
	}

	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("AuditTrailFilter.fxml");
	}
	
	
}
