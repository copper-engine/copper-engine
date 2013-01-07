package de.scoopgmbh.copper.gui.context;

import java.util.ResourceBundle;

import javafx.scene.layout.BorderPane;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.BorderPaneShowFormStrategie;
import de.scoopgmbh.copper.gui.form.Form;
import de.scoopgmbh.copper.gui.form.FxmlForm;
import de.scoopgmbh.copper.gui.ui.login.LoginController;
import de.scoopgmbh.copper.gui.util.MessageProvider;
import de.scoopgmbh.copper.monitor.adapter.CopperMonitorInterface;

public class ApplicationContext {

	private BorderPane mainPane;
	MessageProvider messageProvider;

	public ApplicationContext() {
		mainPane = new BorderPane();
		messageProvider = new MessageProvider(ResourceBundle.getBundle("de.scoopgmbh.copper.gui.message"));
	}

	GuiCopperDataProvider guiCopperDataProvider;
	public void setGuiCopperDataProvider(CopperMonitorInterface copperDataProvider){
		this.guiCopperDataProvider = new GuiCopperDataProvider(copperDataProvider);
	}
	
	public FormContext getFormFactory(){
		if (guiCopperDataProvider==null){
			throw new IllegalStateException("guiCopperDataProvider must initialised");
		}
		return new FormContext(mainPane,guiCopperDataProvider,messageProvider);
	}
	
	public Form<LoginController> createLoginForm(){
		return new FxmlForm<>("login.title", new LoginController(this), messageProvider,  new BorderPaneShowFormStrategie(mainPane));
	}

	public BorderPane getMainPane() {
		return mainPane;
	}
}
