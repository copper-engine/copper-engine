package de.scoopgmbh.copper.gui.context;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ResourceBundle;

import javafx.scene.layout.BorderPane;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.BorderPaneShowFormStrategie;
import de.scoopgmbh.copper.gui.form.Form;
import de.scoopgmbh.copper.gui.form.FxmlForm;
import de.scoopgmbh.copper.gui.ui.login.LoginController;
import de.scoopgmbh.copper.gui.ui.settings.SettingsModel;
import de.scoopgmbh.copper.gui.util.MessageProvider;
import de.scoopgmbh.copper.monitor.adapter.CopperMonitorInterface;
import de.scoopgmbh.copper.monitor.adapter.ServerLogin;

public class ApplicationContext {

	private BorderPane mainPane;
	MessageProvider messageProvider;

	public ApplicationContext() {
		mainPane = new BorderPane();
		mainPane.setId("background");
		messageProvider = new MessageProvider(ResourceBundle.getBundle("de.scoopgmbh.copper.gui.message"));
	}

	GuiCopperDataProvider guiCopperDataProvider;
	public void setGuiCopperDataProvider(CopperMonitorInterface copperDataProvider){
		this.guiCopperDataProvider = new GuiCopperDataProvider(copperDataProvider);
	}
	
	public void setGuiCopperDataProvider(String serverAdress, String user, String password){
		try {
			Registry registry = LocateRegistry.getRegistry(serverAdress,Registry.REGISTRY_PORT);
			ServerLogin serverLogin = (ServerLogin) registry.lookup(ServerLogin.class.getSimpleName());
			
			setGuiCopperDataProvider(serverLogin.login(user, (password!=null?password.hashCode():"")+""));
			getFormFactory().setupGUIStructure();
		} catch (RemoteException | NotBoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	public FormContext getFormFactory(){
		if (guiCopperDataProvider==null){
			throw new IllegalStateException("guiCopperDataProvider must initialised");
		}
		return new FormContext(mainPane,guiCopperDataProvider,messageProvider,settingsModelSinglton);
	}
	
	public Form<LoginController> createLoginForm(){
		return new FxmlForm<>("login.title", new LoginController(this), messageProvider,  new BorderPaneShowFormStrategie(mainPane));
	}

	public BorderPane getMainPane() {
		return mainPane;
	}
	
	SettingsModel settingsModelSinglton = new SettingsModel();
}
