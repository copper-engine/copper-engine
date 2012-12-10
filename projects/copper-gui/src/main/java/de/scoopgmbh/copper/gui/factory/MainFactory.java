package de.scoopgmbh.copper.gui.factory;

import javafx.scene.control.TabPane;
import de.scoopgmbh.copper.gui.form.Form;
import de.scoopgmbh.copper.gui.form.FormManager;
import de.scoopgmbh.copper.gui.form.FxmlForm;
import de.scoopgmbh.copper.gui.ui.dynamicworkflow.DynamicWorkflowController;

public class MainFactory {

	private TabPane mainTabPane;
	private FormManager formManager;
	
	
	public TabPane getMainTabPane() {
		return mainTabPane;
	}

	public MainFactory() {
		this.mainTabPane = new TabPane();
		
		formManager = new FormManager(createDynamicWorkflowForm());
	}

	public Form createDynamicWorkflowForm(){
		return new FxmlForm(mainTabPane, "dynamicworkflow.title", "/de/scoopgmbh/copper/gui/ui/dynamicworkflow/DynamicWorkflow.fxml", new DynamicWorkflowController());
	}
	
	public FormManager getFormManager(){
		return formManager;
	}
}
