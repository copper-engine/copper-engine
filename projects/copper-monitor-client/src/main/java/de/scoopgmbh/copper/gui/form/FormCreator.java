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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;

public abstract class FormCreator{
	public abstract Form<?> createForm();
	
	public String staticTitle;
	public FormCreator(String staticTitle) {
		this.staticTitle = staticTitle;
	}
	
	private Form<?> createFormInternal(){
		Form<?> form = createForm();
		form.setDynamicTitle(staticTitle);
		return form;
	}
	
	public MenuItem createShowFormMenuItem(){
		MenuItem menueItem = MenuItemBuilder
				.create()
				.text(staticTitle)
				.onAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent e) {
						createFormInternal().show();
					}
				}).build();
		return menueItem;
	}
	
	public ButtonBase createShowFormButton(){
		Button button = new Button(staticTitle);
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				createFormInternal().show();
			}
		});
		return button;
	}

	boolean enabled=true;
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean value) {
		enabled=value;
	}
	
}