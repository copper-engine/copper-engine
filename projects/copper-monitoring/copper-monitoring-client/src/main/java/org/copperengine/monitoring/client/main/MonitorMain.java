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
package org.copperengine.monitoring.client.main;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import org.copperengine.monitoring.client.context.ApplicationContext;
import org.copperengine.monitoring.client.ui.settings.CssConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class MonitorMain extends Application {

    static final Logger logger = LoggerFactory.getLogger(MonitorMain.class);

    @Override
    public void start(final Stage primaryStage) { // Stage = window
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/org/copperengine/gui/logo/logo16.png")));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/org/copperengine/gui/logo/logo32.png")));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/org/copperengine/gui/logo/logo64.png")));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/org/copperengine/gui/logo/logo128.png")));

        ApplicationContext applicationContext = new ApplicationContext();
        primaryStage.titleProperty().bind(new SimpleStringProperty("Copper Monitor (server: ").concat(applicationContext.serverAddressProperty().concat(")")));
        new Button(); // Trigger loading of default stylesheet
        final Scene scene = new Scene(applicationContext.getMainPane(), 1280, 800);

        ObservableList<String> stylesheets = scene.getStylesheets();        
        String cssUri = applicationContext.getSettingsModel().cssUri.get();
        CssConfigurator cssConfigurator = new CssConfigurator(cssUri);
        try {
            cssConfigurator.configure(stylesheets);
        } catch (IOException e) {
            logger.error("Failed to apply stylesheets for cssUri " + cssUri, e);
        }
        
        
        primaryStage.setScene(scene);
        primaryStage.show();

        // "--name=value".
        Map<String, String> parameter = getParameters().getNamed();
        String monitorServerAddress = parameter.get("monitorServerAddress");
        String monitorServerUser = parameter.get("monitorServerUser");
        String monitorServerPassword = parameter.get("monitorServerPassword");

        if (!Strings.isNullOrEmpty(monitorServerAddress)) {
            applicationContext.setHttpGuiCopperDataProvider(monitorServerAddress, monitorServerUser, monitorServerPassword);
        } else {
            applicationContext.createLoginForm().show();
        }

        // new Thread(){
        // {
        // setDaemon(true);
        // }
        // @Override
        // public void run() {
        // while(true){
        // PerformanceTracker sceneTracker = PerformanceTracker.getSceneTracker(primaryStage.getScene());
        // System.out.println(sceneTracker.getAverageFPS());
        // sceneTracker.resetAverageFPS();
        //
        // try {
        // Thread.sleep(1000);
        // } catch (InterruptedException e) {
        // throw new RuntimeException(e);
        // }
        //
        // }
        // }
        // }.start();
//        ScenicView.show(scene);
    }

    public static void main(final String[] arguments) {
        // System.setProperty("javafx.animation.fullspeed","true");
        logger.info("Parameter: " + Arrays.asList(arguments));
        Application.launch(arguments);
    }
}