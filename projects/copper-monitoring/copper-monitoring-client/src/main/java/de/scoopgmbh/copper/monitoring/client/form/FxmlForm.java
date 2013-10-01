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
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import de.scoopgmbh.copper.monitoring.client.form.filter.GenericFilterController;
import de.scoopgmbh.copper.monitoring.client.util.MessageProvider;


public class FxmlForm<C extends FxmlController> extends Form<C> {

	private final FXMLLoader fxmlLoader;
	
	public FxmlForm(String dynamicTitle, C controller, MessageProvider messageProvider, ShowFormStrategy<?> showFormStrategie) {
		super(dynamicTitle, showFormStrategie, controller);
		if (controller.getFxmlResource()!=GenericFilterController.EMPTY_DUMMY_URL){
			fxmlLoader = new FXMLLoader(controller.getFxmlResource());
			fxmlLoader.setController(controller);
			fxmlLoader.setResources(messageProvider.getBundle());
		} else {
			fxmlLoader = null;
		}
	}
	
	public FxmlForm(C controller, MessageProvider messageProvider) {
		this("", controller, messageProvider, new EmptyShowFormStrategie());
	}

	@Override
	public Node createContent() {
		if (fxmlLoader != null){
			try {
				return (Parent)fxmlLoader.load();
			} catch (IOException exception) {
				throw new RuntimeException(exception);
			}
		}
		return new Group();
	}
}
