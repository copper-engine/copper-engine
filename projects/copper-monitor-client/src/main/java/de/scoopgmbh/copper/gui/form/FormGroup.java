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
 
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuItemBuilder;
 
public class FormGroup {
    
	ArrayList<Form<?>> forms = new ArrayList<>();
	
	public FormGroup(List<Form<?>> forms){
		this.forms.addAll(forms);
	}
	
	public Menu createMenue(){
		final Menu fileMenu = new Menu("Window");
		for (final Form<?> form: forms){
			MenuItem menueItem = MenuItemBuilder
					.create()
					.text(form.getTitle())
					.onAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent e) {
							form.show();
						}
					}).build();
			fileMenu.getItems().add(menueItem);
		}
		return fileMenu;
	}
	
	public List<Button> createButtonList(){
		ArrayList<Button> result = new ArrayList<>();
		for (final Form<?> form: forms){
			Button button = new Button(form.getTitle());
			button.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					form.show();
				}
			});
			result.add(button);
		}
		return result;
	}
	
}