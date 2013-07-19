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

import java.text.ParseException;
import java.text.SimpleDateFormat;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class DateValidationHelper implements ChangeListener<String> {
	private final TextField field;
	private final String dateFormatString;
	
	public DateValidationHelper(TextField field, String dateFormatString) {
		super();
		this.field = field;
		this.dateFormatString = dateFormatString;
	}

	@Override
	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		boolean parsed=true;
		if (newValue!=null && !newValue.isEmpty()) {
			try {
				new SimpleDateFormat(dateFormatString).parse(newValue);
			} catch (ParseException e) {
				parsed=false;
			}
		}
		if (!parsed){
			if (!field.getStyleClass().contains("error")){
				field.getStyleClass().add("error");
			}
		} else {
			field.getStyleClass().remove("error");
		}
	}
	
	
	public static void addValidation(TextField field, String dateFormatString){
		field.textProperty().addListener(new DateValidationHelper(field,dateFormatString));
	}
}