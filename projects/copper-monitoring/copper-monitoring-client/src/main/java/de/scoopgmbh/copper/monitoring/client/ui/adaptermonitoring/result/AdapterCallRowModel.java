package de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result;

import java.util.Date;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import de.scoopgmbh.copper.monitoring.core.model.AdapterCallInfo;

public class AdapterCallRowModel {
	public final SimpleStringProperty method;
	public final SimpleStringProperty parameter;
	public final SimpleObjectProperty<Date> timestamp;
	
	public AdapterCallRowModel(AdapterCallInfo adapterCall){
		method= new SimpleStringProperty(adapterCall.getMethod());
		parameter= new SimpleStringProperty(adapterCall.getParameter());
		timestamp= new SimpleObjectProperty<Date>(adapterCall.getTimestamp());
	};
}
