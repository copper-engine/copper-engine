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

import java.util.Map;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import de.scoopgmbh.copper.gui.context.ApplicationContext;

public class ClientMain extends Application {

	@Override
	public void start(final Stage primaryStage) { //Stage = window
		primaryStage.setTitle("Copper Monitor");
		ApplicationContext mainFactory = new ApplicationContext();
		new Button(); // Trigger loading of default stylesheet
		final Scene scene = new Scene(mainFactory.getMainPane(), 1200, 768, Color.WHEAT);

		
		scene.getStylesheets().add(this.getClass().getResource("/de/scoopgmbh/copper/gui/css/base.css").toExternalForm());
		
		primaryStage.setScene(scene);
		primaryStage.show();
		
		//"--name=value".
		Map<String, String> parameter = getParameters().getNamed();
		String monitorServerAdress = parameter.get("monitorServerAdress");
		String monitorServerUser = parameter.get("monitorServerUser");
		String monitorServerPassword = parameter.get("monitorServerPassword");

		if (monitorServerAdress==null){
			mainFactory.createLoginForm().show();
			primaryStage.setScene(scene);
			primaryStage.show();
		} else {
			mainFactory.setGuiCopperDataProvider(monitorServerAdress, monitorServerUser, monitorServerPassword);
		}
		
	}

	public static void main(final String[] arguments) {
		Application.launch(arguments);
	}
}