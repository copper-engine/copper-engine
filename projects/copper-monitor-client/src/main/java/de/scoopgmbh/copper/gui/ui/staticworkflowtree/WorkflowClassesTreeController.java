package de.scoopgmbh.copper.gui.ui.staticworkflowtree;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TreeView;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;

public class WorkflowClassesTreeController implements Initializable {
	GuiCopperDataProvider copperDataProvider;

    @FXML //  fx:id="refreshButton"
    private Button refreshButton; // Value injected by FXMLLoader

    @FXML //  fx:id="workflowTree"
    private TreeView<?> workflowTree; // Value injected by FXMLLoader

	public WorkflowClassesTreeController(GuiCopperDataProvider copperDataProvider) {
		super();
		this.copperDataProvider = copperDataProvider;
	}

	@Override
	public void initialize(final URL url, final ResourceBundle rb) {
		

		
	}
}
