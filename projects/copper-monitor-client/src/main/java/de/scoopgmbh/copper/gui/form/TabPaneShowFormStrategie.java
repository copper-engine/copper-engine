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
package de.scoopgmbh.copper.gui.form;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class TabPaneShowFormStrategie extends ShowFormStrategy<TabPane> {

	public TabPaneShowFormStrategie(TabPane component) {
		super(component);
	}

	public void show(Form<?> form){
		Tab tab = new Tab();
		tab.setText("new tab");
		tab.setContent(form.createContent());
		tab.setText(form.getTitle());
		component.getTabs().add(tab);
		component.getSelectionModel().select(tab);
	}
}