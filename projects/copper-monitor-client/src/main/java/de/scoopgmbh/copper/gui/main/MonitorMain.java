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
package de.scoopgmbh.copper.gui.main;

import java.util.Arrays;
import java.util.Map;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import com.google.common.base.Strings;

import de.scoopgmbh.copper.gui.context.ApplicationContext;

public class MonitorMain extends Application {

	@Override
	public void start(final Stage primaryStage) { //Stage = window
		ApplicationContext applicationContext = new ApplicationContext();
		primaryStage.titleProperty().bind(new SimpleStringProperty("Copper Monitor (server: ").concat(applicationContext.serverAdressProperty().concat(")")));
		new Button(); // Trigger loading of default stylesheet
		final Scene scene = new Scene(applicationContext.getMainPane(), 1300, 900, Color.WHEAT);

		scene.getStylesheets().add(this.getClass().getResource("/de/scoopgmbh/copper/gui/css/base.css").toExternalForm());
		
		primaryStage.setScene(scene);
		primaryStage.show();
		
		//"--name=value".
		Map<String, String> parameter = getParameters().getNamed();
		String monitorServerAdress = parameter.get("monitorServerAdress");
		String monitorServerUser = parameter.get("monitorServerUser");
		String monitorServerPassword = parameter.get("monitorServerPassword");

		if (!Strings.isNullOrEmpty(monitorServerAdress) && 
			 Strings.isNullOrEmpty(monitorServerUser) && 
			 Strings.isNullOrEmpty(monitorServerPassword)){
			
			applicationContext.setRMIGuiCopperDataProvider(monitorServerAdress);
		} 
		if (!Strings.isNullOrEmpty(monitorServerAdress) && 
			!Strings.isNullOrEmpty(monitorServerUser) && 
			!Strings.isNullOrEmpty(monitorServerPassword)){
			
			applicationContext.setHttpGuiCopperDataProvider(monitorServerAdress,monitorServerUser,monitorServerPassword);
		} 
		if (Strings.isNullOrEmpty(monitorServerAdress) && 
			Strings.isNullOrEmpty(monitorServerUser) && 
			Strings.isNullOrEmpty(monitorServerPassword)){
			
			applicationContext.createLoginForm().show();
			primaryStage.setScene(scene);
			primaryStage.show();
		} 

//		ScenicView.show(scene);
	}

	public static void main(final String[] arguments) {
		System.out.println(Arrays.asList(arguments));
		Application.launch(arguments);
	}
}