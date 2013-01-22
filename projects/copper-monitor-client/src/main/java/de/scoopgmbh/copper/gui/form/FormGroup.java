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

import javafx.scene.control.ButtonBase;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
 
public class FormGroup extends FormCreator{
	
	ArrayList<FormCreator> forms = new ArrayList<>();
	public FormGroup(String title, List<FormCreator> forms) {
		super(title);
		this.forms.addAll(forms);
	}

	public Menu createMenu(){
		final Menu fileMenu = new Menu("Window");
		for (final FormCreator form: forms){
			fileMenu.getItems().add(form.createShowFormMenuItem());
		}
		return fileMenu;
	}
	
	public List<ButtonBase> createButtonList(){
		ArrayList<ButtonBase> result = new ArrayList<>();
		for (final FormCreator form: forms){
			result.add(form.createShowFormButton());
		}
		return result;
	}
	
	@Override
	public ButtonBase createShowFormButton() {
		MenuButton menuButton = new MenuButton();
		menuButton.setText(staticTitle);
		for (final FormCreator form: forms){
			menuButton.getItems().add(form.createShowFormMenuItem());
		}
		return menuButton;
	}

	@Override
	public Form<?> createForm() {
		return null;//not needed
	}
	
}