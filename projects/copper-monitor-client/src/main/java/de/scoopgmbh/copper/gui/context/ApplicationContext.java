/*
 * Copyright 2002-2012 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.scoopgmbh.copper.gui.context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import de.scoopgmbh.copper.gui.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.gui.form.BorderPaneShowFormStrategie;
import de.scoopgmbh.copper.gui.form.Form;
import de.scoopgmbh.copper.gui.form.FxmlForm;
import de.scoopgmbh.copper.gui.ui.login.LoginController;
import de.scoopgmbh.copper.gui.ui.settings.AuditralColorMapping;
import de.scoopgmbh.copper.gui.ui.settings.SettingsModel;
import de.scoopgmbh.copper.gui.util.MessageProvider;
import de.scoopgmbh.copper.monitor.adapter.CopperMonitorInterface;
import de.scoopgmbh.copper.monitor.adapter.ServerLogin;

public class ApplicationContext {

	private static final String SETTINGS_KEY = "settings";
	private BorderPane mainPane;
	MessageProvider messageProvider;
	SettingsModel settingsModelSinglton;
	
	public ApplicationContext() {
		mainPane = new BorderPane();
		mainPane.setId("background");
		messageProvider = new MessageProvider(ResourceBundle.getBundle("de.scoopgmbh.copper.gui.message"));
		
		final Preferences prefs = Preferences.userRoot().node("de.scoopgmbh.coppermonitor");
		
		SettingsModel defaultSettings = new SettingsModel();
		AuditralColorMapping newItem = new AuditralColorMapping();
		newItem.color.setValue(Color.rgb(240, 40, 40));
		newItem.loglevelRegEx.setValue("1");
		defaultSettings.auditralColorMappings.add(newItem);
		byte[] defaultModelbytes;
		try (ByteArrayOutputStream os=new ByteArrayOutputStream()){
			ObjectOutputStream o = new ObjectOutputStream(os);
			o.writeObject(defaultSettings);
			defaultModelbytes = os.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		try (ByteArrayInputStream is=new ByteArrayInputStream(prefs.getByteArray(SETTINGS_KEY, defaultModelbytes))){
			ObjectInputStream o = new ObjectInputStream(is);
			settingsModelSinglton = (SettingsModel)o.readObject();
		} catch (InvalidClassException e){
			e.printStackTrace();
			settingsModelSinglton=defaultSettings;
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		} 

    	Runtime.getRuntime().addShutdownHook( 
    		new Thread(
    			new Runnable() {
    				public void run() {
    					try (ByteArrayOutputStream os=new ByteArrayOutputStream()){
    						ObjectOutputStream o = new ObjectOutputStream(os);
    						o.writeObject(settingsModelSinglton);
    						prefs.putByteArray(SETTINGS_KEY, os.toByteArray());
    					} catch (IOException e) {
    						throw new RuntimeException(e);
    					}
    				}	
    			}
    		)
    	);
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

}
