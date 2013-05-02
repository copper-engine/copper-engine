/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package de.scoopgmbh.copper.monitoring.client.context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.BorderPaneShowFormStrategie;
import de.scoopgmbh.copper.monitoring.client.form.Form;
import de.scoopgmbh.copper.monitoring.client.form.FxmlForm;
import de.scoopgmbh.copper.monitoring.client.ui.login.LoginController;
import de.scoopgmbh.copper.monitoring.client.ui.settings.AuditralColorMapping;
import de.scoopgmbh.copper.monitoring.client.ui.settings.SettingsModel;
import de.scoopgmbh.copper.monitoring.client.util.MessageProvider;
import de.scoopgmbh.copper.monitoring.core.CopperMonitorInterface;

public class ApplicationContext {

	private static final String SETTINGS_KEY = "settings";
	private BorderPane mainPane;
	private StackPane mainStackPane;
	MessageProvider messageProvider;
	SettingsModel settingsModelSinglton;
	
	private SimpleStringProperty serverAdress= new SimpleStringProperty();
	public SimpleStringProperty serverAdressProperty() {
		return serverAdress;
	}
	
	public ApplicationContext() {
		mainStackPane = new StackPane();
		mainPane = new BorderPane();
		mainPane.setId("background");//important for css
		mainStackPane.getChildren().add(mainPane);
		messageProvider = new MessageProvider(ResourceBundle.getBundle("de.scoopgmbh.copper.gui.message"));
		
		final Preferences prefs = Preferences.userRoot().node("de.scoopgmbh.coppermonitor");
		
		SettingsModel defaultSettings = new SettingsModel();
		AuditralColorMapping newItem = new AuditralColorMapping();
		newItem.color.setValue(Color.rgb(240, 40, 40));
		newItem.loglevelRegEx.setValue("1");
		defaultSettings.auditralColorMappings.add(newItem);
		byte[] defaultModelbytes;
		ByteArrayOutputStream os=null;
		try{
		    os=new ByteArrayOutputStream();
			ObjectOutputStream o = new ObjectOutputStream(os);
			o.writeObject(defaultSettings);
			defaultModelbytes = os.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (os!=null){
				try {
					os.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		settingsModelSinglton=defaultSettings;
		ByteArrayInputStream is=null;
		try{
			is = new ByteArrayInputStream(prefs.getByteArray(SETTINGS_KEY, defaultModelbytes));
			ObjectInputStream o = new ObjectInputStream(is);
			Object object= o.readObject();
			if (object instanceof SettingsModel){
				settingsModelSinglton = (SettingsModel)object;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (is!=null){
				try {
					is.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

    	Runtime.getRuntime().addShutdownHook( 
    		new Thread(
    			new Runnable() {
    				@Override
					public void run() {
    					ByteArrayOutputStream os=null;
    					try{
    						os = new ByteArrayOutputStream();
    						ObjectOutputStream o = new ObjectOutputStream(os);
    						o.writeObject(settingsModelSinglton);
    						prefs.putByteArray(SETTINGS_KEY, os.toByteArray());
    					} catch (IOException e) {
    						throw new RuntimeException(e);
    					} finally {
    						if (os!=null){
    							try {
									os.close();
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
    						}
    					}
    				}	
    			}
    		)
    	);
	}
	
	public void setRMIGuiCopperDataProvider(String serverAdress){
		try {
			Registry registry = LocateRegistry.getRegistry(serverAdress,Registry.REGISTRY_PORT);
			CopperMonitorInterface copperMonitor = (CopperMonitorInterface) registry.lookup(CopperMonitorInterface.class.getSimpleName());
			setGuiCopperDataProvider(copperMonitor,serverAdress);
			setGuiCopperDataProvider(copperMonitor,serverAdress);
		} catch (AccessException e) {
			throw new RuntimeException(e);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		} catch (NotBoundException e) {
			throw new RuntimeException(e);
		}
	}

	GuiCopperDataProvider guiCopperDataProvider;
	public void setGuiCopperDataProvider(CopperMonitorInterface copperDataProvider, String serverAdress){
		this.serverAdress.set(serverAdress);
		this.guiCopperDataProvider = new GuiCopperDataProvider(copperDataProvider);
		getFormFactorySingelton().setupGUIStructure();
	}
	
	public void setHttpGuiCopperDataProvider(final String serverAdress, final String user, final String password){
		final ProgressIndicator progressIndicator = new ProgressIndicator();
		progressIndicator.setMaxSize(300, 300);
		mainStackPane.getChildren().add(progressIndicator);
		final Label label = new Label("connecting");
		label.setWrapText(true);
		mainStackPane.getChildren().add(label);
		Thread th = new Thread(){
			@Override
			public void run() {
				try {
					HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
					httpInvokerProxyFactoryBean.setServiceInterface(CopperMonitorInterface.class);
					String completeServiceAdress= serverAdress;
					if (!completeServiceAdress.endsWith("/")){
						completeServiceAdress= completeServiceAdress+"/";
					}
					completeServiceAdress = completeServiceAdress+"copperMonitorInterface";
					httpInvokerProxyFactoryBean.setServiceUrl(completeServiceAdress);
					httpInvokerProxyFactoryBean.setHttpInvokerRequestExecutor(new CommonsHttpInvokerRequestExecutor());
					httpInvokerProxyFactoryBean.afterPropertiesSet();
					
					final CopperMonitorInterface copperMonitorInterface = (CopperMonitorInterface)httpInvokerProxyFactoryBean.getObject();
					try {
						copperMonitorInterface.doLogin(user, password);
					} catch (RemoteException e) {
						throw new RuntimeException(e);
					}
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							mainStackPane.getChildren().remove(progressIndicator);
							mainStackPane.getChildren().remove(label);
							setGuiCopperDataProvider(copperMonitorInterface,serverAdress);
						}
					});
				} catch (final Exception e){
					e.printStackTrace();
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							mainStackPane.getChildren().remove(progressIndicator);
							label.setText("Can't Connect: \n"+e.getMessage());
						}
					});
				}
				
			}
		};
		th.setDaemon(true);
		th.start();
	}
	
	FormContext formContext;
	public FormContext getFormFactorySingelton(){
		if (guiCopperDataProvider==null){
			throw new IllegalStateException("guiCopperDataProvider must initialised");
		}
		if (formContext==null){
			formContext = new FormContext(mainPane,guiCopperDataProvider,messageProvider,settingsModelSinglton);
		}
		return formContext;
	}
	
	public Form<LoginController> createLoginForm(){
		return new FxmlForm<LoginController>("login.title", new LoginController(this), messageProvider,  new BorderPaneShowFormStrategie(mainPane));
	}

	public Pane getMainPane() {
		return mainStackPane;
	}

}
