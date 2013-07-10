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