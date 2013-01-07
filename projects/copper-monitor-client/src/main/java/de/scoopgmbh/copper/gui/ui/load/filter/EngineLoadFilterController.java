package de.scoopgmbh.copper.gui.ui.load.filter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import de.scoopgmbh.copper.gui.form.FxmlController;
import de.scoopgmbh.copper.gui.form.filter.FilterController;

public class EngineLoadFilterController implements Initializable, FilterController<EngineLoadFilterModel>, FxmlController {
	EngineLoadFilterModel model;


    @FXML //  fx:id="showRunningCheckBox"
    private CheckBox showRunningCheckBox; // Value injected by FXMLLoader


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert showRunningCheckBox != null : "fx:id=\"showRunningCheckBox\" was not injected: check your FXML file 'EngineLoadFilter.fxml'.";


        model = new EngineLoadFilterModel();
        showRunningCheckBox.selectedProperty().bindBidirectional(model.showRunning);
	}

	@Override
	public EngineLoadFilterModel getFilter() {
		return model;
	}

	@Override
	public URL getFxmlRessource() {
		return getClass().getResource("EngineLoadFilter.fxml");
	}
	
	
}
