package de.scoopgmbh.copper.gui.util;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.StringConverter;

public class ConvertingStringProperty<T> extends SimpleStringProperty{
	
	public ConvertingStringProperty(final Property<T> property, final StringConverter<T> converter){
		this.set(converter.toString(property.getValue()));
		this.addListener(new ChangeListener<String>() { //TODO der Listener ist vermutlich schlecht für performance besser passende Methode überschreiebn
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				property.setValue(converter.fromString(newValue));
			}
		});
	}
	
	public ConvertingStringProperty(final T row, final StringConverter<T> converter){
		this.set(converter.toString(row));
		this.addListener(new ChangeListener<String>() { 
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				converter.fromString(newValue);
			}
		});
	}
	

}
