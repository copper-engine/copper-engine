/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
package org.copperengine.monitoring.client.context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.shiro.spring.remoting.SecureRemoteInvocationFactory;
import org.copperengine.monitoring.client.adapter.GuiCopperDataProvider;
import org.copperengine.monitoring.client.form.BorderPaneShowFormStrategie;
import org.copperengine.monitoring.client.form.Form;
import org.copperengine.monitoring.client.form.FxmlForm;
import org.copperengine.monitoring.client.form.dialog.DefaultInputDialogCreator;
import org.copperengine.monitoring.client.form.dialog.InputDialogCreator;
import org.copperengine.monitoring.client.form.issuereporting.IssueReporter;
import org.copperengine.monitoring.client.form.issuereporting.MessageAndLogIssueReporter;
import org.copperengine.monitoring.client.ui.login.LoginController;
import org.copperengine.monitoring.client.ui.settings.AuditralColorMapping;
import org.copperengine.monitoring.client.ui.settings.SettingsModel;
import org.copperengine.monitoring.client.util.ComponentUtil;
import org.copperengine.monitoring.client.util.MessageProvider;
import org.copperengine.monitoring.core.CopperMonitoringService;
import org.copperengine.monitoring.core.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.support.DefaultRemoteInvocationFactory;
import org.springframework.remoting.support.RemoteInvocationFactory;
import org.springframework.util.StringUtils;

public class ApplicationContext {

    Logger logger = LoggerFactory.getLogger(ApplicationContext.class);

    private static final String SETTINGS_KEY = "settings";
    protected BorderPane mainPane;
    protected StackPane mainStackPane;
    protected MessageProvider messageProvider;
    protected SettingsModel settingsModelSingleton;

    protected SimpleStringProperty serverAdress = new SimpleStringProperty();

    public SimpleStringProperty serverAdressProperty() {
        return serverAdress;
    }

    public ApplicationContext() {
        mainStackPane = new StackPane();
        mainPane = new BorderPane();
        mainPane.setId("background");// important for css
        mainStackPane.getChildren().add(mainPane);
        messageProvider = new MessageProvider(ResourceBundle.getBundle("org.copperengine.gui.message"));

        final Preferences prefs = Preferences.userRoot().node("org.copperengine.coppermonitor");

        SettingsModel defaultSettings = new SettingsModel();
        AuditralColorMapping newItem = new AuditralColorMapping();
        newItem.color.setValue(Color.rgb(255, 128, 128));
        newItem.loglevelRegEx.setValue("1");
        defaultSettings.auditralColorMappings.add(newItem);
        byte[] defaultModelbytes;
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(os);
            o.writeObject(defaultSettings);
            defaultModelbytes = os.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        settingsModelSingleton = defaultSettings;
        ByteArrayInputStream is = null;
        try {
            is = new ByteArrayInputStream(prefs.getByteArray(SETTINGS_KEY, defaultModelbytes));
            ObjectInputStream o = new ObjectInputStream(is);
            Object object = o.readObject();
            if (object instanceof SettingsModel) {
                settingsModelSingleton = (SettingsModel) object;
            }
        } catch (Exception e) {
            logger.error("", e);
            getIssueReporterSingleton().reportWarning("Can't load settings from (Preferences: " + prefs + ") use defaults instead", e);
        } finally {
            if (is != null) {
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
                                ByteArrayOutputStream os = null;
                                try {
                                    os = new ByteArrayOutputStream();
                                    ObjectOutputStream o = new ObjectOutputStream(os);
                                    o.writeObject(settingsModelSingleton);
                                    prefs.putByteArray(SETTINGS_KEY, os.toByteArray());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                } finally {
                                    if (os != null) {
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

    public void setGuiCopperDataProvider(CopperMonitoringService copperDataProvider, String serverAdress, String sessionId) {
        this.serverAdress.set(serverAdress);
        this.guiCopperDataProvider = new GuiCopperDataProvider(copperDataProvider);
        getFormContextSingleton().setupGUIStructure();
    }

    public void setHttpGuiCopperDataProvider(final String serverAdressParam, final String user, final String password) {
        ComponentUtil.executeWithProgressDialogInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    connect(serverAdressParam, user, password);
                } catch (final Exception e) {
                    logger.error("", e);
                    getIssueReporterSingleton().reportError("Can't Connect: \n" + e.getMessage(), e);
                }
            }
        }, mainStackPane, "connecting");
    }

    protected void connect(final String serverAdressParam, final String user, final String password) {
        boolean secureConnect = StringUtils.hasText(user) && StringUtils.hasText(password);
        String serverAdress = serverAdressParam;
        if (!serverAdress.endsWith("/")) {
            serverAdress = serverAdress + "/";
        }

        final LoginService loginService;
        final CommonsHttpInvokerRequestExecutor httpInvokerRequestExecutor = new CommonsHttpInvokerRequestExecutor();
        DefaultHttpMethodRetryHandler retryHandler = new DefaultHttpMethodRetryHandler(10, false);
        httpInvokerRequestExecutor.getHttpClient().getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryHandler);
        httpInvokerRequestExecutor.getHttpClient().getParams().setSoTimeout(1000 * 60 * 5);
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
        if (secureConnect) {
            try {
                sessionId = loginService.doLogin(user, password);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        } else {
            sessionId = "";
        }

        if (sessionId == null) {
            getIssueReporterSingleton().reportWarning("Invalid user/password", null, new Runnable() {
                @Override
                public void run() {
                    createLoginForm().show();
                }
            });
        } else {
            HttpInvokerProxyFactoryBean httpInvokerProxyFactoryBean = new HttpInvokerProxyFactoryBean();
            httpInvokerProxyFactoryBean.setServiceUrl(serverAdress + "copperMonitoringService");
            httpInvokerProxyFactoryBean.setServiceInterface(CopperMonitoringService.class);
            RemoteInvocationFactory remoteInvocationFactory = secureConnect ?
                    new SecureRemoteInvocationFactory(sessionId) : new DefaultRemoteInvocationFactory();
            httpInvokerProxyFactoryBean.setRemoteInvocationFactory(remoteInvocationFactory);
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

    protected FormContext formContext;

    public FormContext getFormContextSingleton() {
        if (guiCopperDataProvider == null) {
            throw new IllegalStateException("guiCopperDataProvider must be initialized");
        }
        if (formContext == null) {
            formContext = new FormContext(mainPane, guiCopperDataProvider, messageProvider, settingsModelSingleton, getIssueReporterSingleton(), getInputDialogCreator());
        }
        return formContext;
    }

    protected InputDialogCreator getInputDialogCreator() {
        return new DefaultInputDialogCreator(mainStackPane);
    }

    private IssueReporter issueReporter;

    private IssueReporter getIssueReporterSingleton() {
        if (issueReporter == null) {
            issueReporter = createIssueReporter();
        }
        return issueReporter;
    }

    /**
     * template method to create custom {@link IssueReporter}
     */
    protected IssueReporter createIssueReporter() {
        return new MessageAndLogIssueReporter(mainStackPane);
    }

    public void resetFormContext() {
        formContext = null;
    }

    protected FxmlForm<LoginController> loginForm;

    public Form<LoginController> createLoginForm() {
        if (loginForm == null) {
            loginForm = new FxmlForm<LoginController>("login.title", new LoginController(this, settingsModelSingleton), messageProvider, new BorderPaneShowFormStrategie(mainPane));
        }
        return loginForm;
    }

    public Pane getMainPane() {
        return mainStackPane;
    }

}
