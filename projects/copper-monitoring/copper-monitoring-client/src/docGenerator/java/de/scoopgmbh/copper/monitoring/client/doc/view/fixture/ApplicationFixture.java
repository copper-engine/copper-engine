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
package de.scoopgmbh.copper.monitoring.client.doc.view.fixture;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ApplicationFixture extends Application {

	private static BorderPane pane;
	private static Stage stage;
	
	@Override
	public void start(final Stage stage) { 
		pane = new BorderPane();
		
		final Scene scene = new Scene(pane, 1300, 900, Color.WHEAT);
	    stage.setWidth(1167);
	    stage.setHeight(800);
	    
	    scene.getStylesheets().add(this.getClass().getResource("/de/scoopgmbh/copper/gui/css/base.css").toExternalForm());
	    
	    stage.setScene(scene);
	    stage.show();
	    
	    ApplicationFixture.stage = stage;
	}
	
	public static BorderPane getPane(){
		return pane;
	}

	public static void launchWorkaround(){
		Application.launch(new String[]{});
	}

	public static Stage getStage() {
		return stage;
	}

	
}