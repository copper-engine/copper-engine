package de.scoopgmbh.copper.monitoring.client.form.filter.defaultfilter;

import java.util.Date;

import javafx.beans.property.SimpleObjectProperty;

public class FromToFilterModel {
	public final SimpleObjectProperty<Date> from = new SimpleObjectProperty<Date>();
	public final SimpleObjectProperty<Date> to = new SimpleObjectProperty<Date>();
}
