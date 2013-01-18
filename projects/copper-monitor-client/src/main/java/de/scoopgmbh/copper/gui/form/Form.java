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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
import de.scoopgmbh.copper.gui.util.MessageProvider;

/**
 *
 * @param <T> target component to display the form
 */
public abstract class Form<C> {

	private final String titleTextKey;
	protected final MessageProvider messageProvider;
	private final ShowFormStrategy<?> showFormStrategie;
	protected final C controller;
	
	public Form(String titleTextKey, MessageProvider messageProvider, ShowFormStrategy<?> showFormStrategie, C controller) {
		super();
		this.titleTextKey = titleTextKey;
		this.messageProvider = messageProvider;
		this.showFormStrategie = showFormStrategie;
		this.controller = controller;
	}

	public String getTitle(){
		return messageProvider.getText(titleTextKey);
	}
	
	public void show(){
		showFormStrategie.show(this);
	}
	
	public abstract Node createContent();
	
	public C getController(){
		return controller;
	}
	
	public MenuItem createShowFormMenuItem(){
		MenuItem menueItem = MenuItemBuilder
				.create()
				.text(getTitle())
				.onAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent e) {
						show();
					}
				}).build();
		return menueItem;
	}
	
	public ButtonBase createShowFormButton(){
		Button button = new Button(getTitle());
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				show();
			}
		});
		return button;
	}
	
	
;
	
	
}
