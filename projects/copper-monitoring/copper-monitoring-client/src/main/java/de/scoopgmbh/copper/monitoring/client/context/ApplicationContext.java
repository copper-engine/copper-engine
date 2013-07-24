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
import java.lang.Thread.UncaughtExceptionHandler;
import java.rmi.RemoteException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.shiro.spring.remoting.SecureRemoteInvocationFactory;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import com.google.common.base.Throwables;

import de.scoopgmbh.copper.monitoring.client.adapter.GuiCopperDataProvider;
import de.scoopgmbh.copper.monitoring.client.form.BorderPaneShowFormStrategie;
import de.scoopgmbh.copper.monitoring.client.form.Form;
import de.scoopgmbh.copper.monitoring.client.form.FxmlForm;
import de.scoopgmbh.copper.monitoring.client.ui.login.LoginController;
import de.scoopgmbh.copper.monitoring.client.ui.settings.AuditralColorMapping;
import de.scoopgmbh.copper.monitoring.client.ui.settings.SettingsModel;
import de.scoopgmbh.copper.monitoring.client.util.CSSHelper;
import de.scoopgmbh.copper.monitoring.client.util.ComponentUtil;
import de.scoopgmbh.copper.monitoring.client.util.MessageProvider;
import de.scoopgmbh.copper.monitoring.core.CopperMonitoringService;
import de.scoopgmbh.copper.monitoring.core.LoginService;

public class ApplicationContext {

	private static final String SETTINGS_KEY = "settings";
	private BorderPane mainPane;
	private StackPane mainStackPane;
	private MessageProvider messageProvider;
	private SettingsModel settingsModelSingleton;
	
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
		newItem.color.setValue(Color.rgb(255, 128, 128));
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
		
		settingsModelSingleton=defaultSettings;
		ByteArrayInputStream is=null;
		try{
			is = new ByteArrayInputStream(prefs.getByteArray(SETTINGS_KEY, defaultModelbytes));
			ObjectInputStream o = new ObjectInputStream(is);
			Object object= o.readObject();
			if (object instanceof SettingsModel){
				settingsModelSingleton = (SettingsModel)object;
			}
		} catch (Exception e) {
			e.printStackTrace();
			showWarningMessage("Can't load settings from (Preferences: "+prefs+") use defaults instead",e);
		} finally {
			if (is!=null){
				try {
					is.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				e.printStackTrace();
				showErrorMessage(t.getName(), e);
			}
		});

    	Runtime.getRuntime().addShutdownHook( 
    		new Thread(
    			new Runnable() {
    				@Override
					public void run() {
    					ByteArrayOutputStream os=null;
    					try{
    						os = new ByteArrayOutputStream();
    						ObjectOutputStream o = new ObjectOutputStream(os);
    						o.writeObject(settingsModelSingleton);
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
	
	protected GuiCopperDataProvider guiCopperDataProvider;
	public void setGuiCopperDataProvider(CopperMonitoringService copperDataProvider, String serverAdress, String sessionId){
		this.serverAdress.set(serverAdress);
		this.guiCopperDataProvider = new GuiCopperDataProvider(copperDataProvider);
		getFormContextSingleton().setupGUIStructure();
	}
	
	public void setHttpGuiCopperDataProvider(final String serverAdressParam, final String user, final String password){
		ComponentUtil.executeWithProgressDialogInBackground(new Runnable() {
			@Override
			public void run() {
				try {
					connect(serverAdressParam,user,password);
				} catch (final Exception e){
					e.printStackTrace();
					showErrorMessage("Can't Connect: \n"+e.getMessage(),e);
				}
			}
		}, mainStackPane, "connecting");
	}
		

	protected void connect(final String serverAdressParam, final String user, final String password) {
		String serverAdress = serverAdressParam;
		if (!serverAdress.endsWith("/")) {
			serverAdress = serverAdress + "/";
		}

		final LoginService loginService;
		final CommonsHttpInvokerRequestExecutor httpInvokerRequestExecutor = new CommonsHttpInvokerRequestExecutor();
		DefaultHttpMethodRetryHandler retryHandler = new DefaultHttpMethodRetryHandler(10, false);
		httpInvokerRequestExecutor.getHttpClient().getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryHandler);
		httpInvokerRequestExecutor.getHttpClient().getParams().setSoTimeout(1000*60*5);
		{
			HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
			httpInvokerProxyFactoryBean.setServiceUrl(serverAdress + "copperMonitoringService");
			httpInvokerProxyFactoryBean.setServiceInterface(LoginService.class);
			httpInvokerProxyFactoryBean.setServiceUrl(serverAdress + "loginService");
			httpInvokerProxyFactoryBean.afterPropertiesSet();
			httpInvokerProxyFactoryBean.setHttpInvokerRequestExecutor(httpInvokerRequestExecutor);
			loginService = (LoginService) httpInvokerProxyFactoryBean.getObject();
		}

		final String sessionId;
		try {
			sessionId = loginService.doLogin(user, password);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}

		if (sessionId == null) {
			showWarningMessage("Invalid user/password", null, new Runnable() {
				@Override
				public void run() {
					createLoginForm().show();
				}
			});
		} else {
			HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
			httpInvokerProxyFactoryBean.setServiceUrl(serverAdress + "copperMonitoringService");
			httpInvokerProxyFactoryBean.setServiceInterface(CopperMonitoringService.class);
			httpInvokerProxyFactoryBean.setRemoteInvocationFactory(new SecureRemoteInvocationFactory(sessionId));
			httpInvokerProxyFactoryBean.setHttpInvokerRequestExecutor(httpInvokerRequestExecutor);
			httpInvokerProxyFactoryBean.afterPropertiesSet();
			final CopperMonitoringService copperMonitoringService = (CopperMonitoringService) httpInvokerProxyFactoryBean.getObject();

			final String serverAdressFinal = serverAdress;
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					setGuiCopperDataProvider(copperMonitoringService, serverAdressFinal, sessionId);
				}
			});
		}

	}
	
	private FormContext formContext;
	public FormContext getFormContextSingleton(){
		if (guiCopperDataProvider==null){
			throw new IllegalStateException("guiCopperDataProvider must be initialized");
		}
		if (formContext==null){
			formContext = new FormContext(mainPane,guiCopperDataProvider,messageProvider,settingsModelSingleton);
		}
		return formContext;
	}
	public void resetFormContext() {
		formContext = null;
	}
	
	private FxmlForm<LoginController> fxmlForm;
	public Form<LoginController> createLoginForm(){
		if (fxmlForm==null){
			fxmlForm = new FxmlForm<LoginController>("login.title", new LoginController(this,settingsModelSingleton), messageProvider,  new BorderPaneShowFormStrategie(mainPane));
		}
		return fxmlForm;
	}

	public Pane getMainPane() {
		return mainStackPane;
	}
	
	
	public void showMessage(final String message, final Throwable e, final Color backColor, final ImageView icon, final Runnable okOnAction){
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				final Pane backShadow = new Pane();
				backShadow.setStyle("-fx-background-color: "+CSSHelper.toCssColor(backColor)+";");
				mainStackPane.getChildren().add(backShadow);
				
				String blackOrWhiteDependingFromBack ="ladder("+CSSHelper.toCssColor(backColor)+", white 49%, black 50%);";
				
				final VBox back = new VBox(3);
				StackPane.setMargin(back, new Insets(150));
				back.setStyle("-fx-border-color: "+blackOrWhiteDependingFromBack +"; -fx-border-width: 1px; -fx-padding: 3; -fx-background-color: derive("+CSSHelper.toCssColor(backColor)+",-50%);");
				back.setAlignment(Pos.CENTER_RIGHT);
				final Label label = new Label(message);
				label.prefWidthProperty().bind(mainStackPane.widthProperty());
				StackPane.setMargin(back, new Insets(150));
				label.setStyle("-fx-text-fill: "+blackOrWhiteDependingFromBack +";");
				label.setWrapText(true);
				label.setGraphic(icon);
				back.getChildren().add(label);
				
				final TextArea area = new TextArea();
				area.setPrefRowCount(10);
				if (e!=null){
					area.setText(Throwables.getStackTraceAsString(e));
				}
				area.setOpacity(0.4);
				area.setEditable(false);
				VBox.setVgrow(area, Priority.ALWAYS);
				back.getChildren().add(area);
				area.getStyleClass().add("consoleFont");
				
				ContextMenu menue = new ContextMenu();
				MenuItem item = new MenuItem("copy to clipboard");
				item.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						final Clipboard clipboard = Clipboard.getSystemClipboard();
					    final ClipboardContent content = new ClipboardContent();
					    content.putString(area.getText());
					    clipboard.setContent(content);
					}
				});
				menue.getItems().add(item);
				area.setContextMenu(menue);
				
				Button ok = new Button("OK");
				ok.setPrefWidth(100);
				ok.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						mainStackPane.getChildren().remove(back);
						mainStackPane.getChildren().remove(backShadow);
						if (okOnAction!=null){
							okOnAction.run();
						}
					}
				});
				back.getChildren().add(ok);
				
				mainStackPane.getChildren().add(back);
			}
		});
	}
	
	public void showErrorMessage(String message, Throwable e){
		showErrorMessage(message,e,null);
	}
	
	public void showErrorMessage(String message, Throwable e, Runnable okOnACtion){
		showMessage(message,e,Color.rgb(255,0,0,0.55), new ImageView(getClass().getResource("/de/scoopgmbh/copper/gui/icon/error.png").toExternalForm()),okOnACtion);
	}
	
	public void showWarningMessage(String message, Throwable e, Runnable okOnACtion){
		showMessage(message,e,Color.rgb(255,200,90,0.75), new ImageView(getClass().getResource("/de/scoopgmbh/copper/gui/icon/warning.png").toExternalForm()),okOnACtion);
	}
	
	public void showWarningMessage(String message, Throwable e){
		showWarningMessage(message,e,null);
	}

}
