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
package de.scoopgmbh.copper.monitoring.client.form;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import de.scoopgmbh.copper.monitoring.client.form.filter.GenericFilterController;
import de.scoopgmbh.copper.monitoring.client.util.MessageProvider;


public class FxmlForm<C extends FxmlController> extends Form<C> {


	private final MessageProvider messageProvider;
	public FxmlForm(String dynamicTitle, C controller, MessageProvider messageProvider, ShowFormStrategy<?> showFormStrategie) {
		super(dynamicTitle, showFormStrategie, controller);
		this.messageProvider = messageProvider;
	}
	
	public FxmlForm(C controller, MessageProvider messageProvider) {
		super("", new EmptyShowFormStrategie(), controller);
		this.messageProvider = messageProvider;
	}

	@Override
	public Node createContent() {
		if (controller.getFxmlRessource()!=GenericFilterController.EMPTY_DUMMY_URL){
			FXMLLoader fxmlLoader = new FXMLLoader(controller.getFxmlRessource());
			fxmlLoader.setController(controller);
			fxmlLoader.setResources(messageProvider.getBundle());
			try {
				Parent load = (Parent) fxmlLoader.load();
				return load;
			} catch (IOException exception) {
				throw new RuntimeException(exception);
			}
		}
		return new Region();
	}
	
	
}
