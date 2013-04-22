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
package de.scoopgmbh.copper.gui.form;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @param <T> target component to display the form
 */
public abstract class Form<C> implements Widget {

	private final SimpleStringProperty dynamicTitle;
	private final ShowFormStrategy<?> showFormStrategie;
	protected final C controller;
	

	public Form(String dynamicTitle, ShowFormStrategy<?> showFormStrategie, C controller) {
		super();
		this.dynamicTitle = new SimpleStringProperty(dynamicTitle);
		this.showFormStrategie = showFormStrategie;
		this.controller = controller;
	}

	public SimpleStringProperty dynamicTitleProperty(){
		return dynamicTitle;
	}
	
	public void show(){
		showFormStrategie.show(this);
	}

	public C getController(){
		return controller;
	}

	public void setDynamicTitle(String staticTitle) {
		dynamicTitleProperty().setValue(staticTitle);
	}
	
}
