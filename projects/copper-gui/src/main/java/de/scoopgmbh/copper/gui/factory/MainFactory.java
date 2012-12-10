package de.scoopgmbh.copper.gui.factory;

import java.util.ResourceBundle;

import javafx.scene.control.TabPane;
import de.scoopgmbh.copper.gui.form.Form;
import de.scoopgmbh.copper.gui.form.FormManager;
import de.scoopgmbh.copper.gui.form.FxmlForm;
import de.scoopgmbh.copper.gui.ui.dynamicworkflow.DynamicWorkflowController;
import de.scoopgmbh.copper.gui.util.MessageProvider;

public class MainFactory {

	private TabPane mainTabPane;
	private FormManager formManager;
	MessageProvider messageProvider;

	public TabPane getMainTabPane() {
		return mainTabPane;
	}

	public MainFactory() {
		this.mainTabPane = new TabPane();
		messageProvider = new MessageProvider(ResourceBundle.getBundle("de.scoopgmbh.copper.gui.message"));
		
		formManager = new FormManager(createDynamicWorkflowForm());
	}

	public Form createDynamicWorkflowForm(){
		return new FxmlForm(mainTabPane, "dynamicworkflow.title", "/de/scoopgmbh/copper/gui/ui/dynamicworkflow/DynamicWorkflow.fxml", new DynamicWorkflowController(),messageProvider);
	}
	
	public FormManager getFormManager(){
		return formManager;
	}
}
