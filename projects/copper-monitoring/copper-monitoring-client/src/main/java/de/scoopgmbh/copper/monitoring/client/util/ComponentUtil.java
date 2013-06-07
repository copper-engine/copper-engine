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
package de.scoopgmbh.copper.monitoring.client.util;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;

public class ComponentUtil {
	
	public static ProgressIndicator createProgressIndicator(){
		ProgressIndicator indicator = new ProgressIndicator();
		
		indicator.setStyle("-fx-background-color: rgba(230,230,230,0.7);" +
					"-fx-padding: 5em 5em 5em 5em;");

		return indicator;
	}
	
	
	public static void executeWithProgressDialogInBackground(final Runnable runnable, final StackPane target, final String text){
		Thread th = new Thread(){
			@Override
			public void run() {
				final ProgressIndicator progressIndicator = new ProgressIndicator();
				final Label label = new Label(text);
				try {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							progressIndicator.setMaxSize(300, 300);
							target.getChildren().add(progressIndicator);
							label.setWrapText(true);
							target.getChildren().add(label);
						}
					});
					runnable.run();
				} finally {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							target.getChildren().remove(progressIndicator);
							target.getChildren().remove(label);
						}
					});	
				}
			}
		};
		th.setDaemon(true);
		th.start();
	}

}
