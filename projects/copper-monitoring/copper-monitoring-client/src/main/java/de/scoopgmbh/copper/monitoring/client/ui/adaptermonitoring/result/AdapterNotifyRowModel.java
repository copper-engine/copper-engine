package de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result;

import java.util.Date;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfNotifyInfo;

public class AdapterNotifyRowModel {
	public final SimpleStringProperty correlationId;
	public final SimpleStringProperty message;
	public final SimpleObjectProperty<Date> timestamp;
	
	public AdapterNotifyRowModel(AdapterWfNotifyInfo  adapterWfNotifyInfo){
		correlationId= new SimpleStringProperty(adapterWfNotifyInfo.getCorrelationId());
		message= new SimpleStringProperty(adapterWfNotifyInfo.getMessage());
		timestamp= new SimpleObjectProperty<Date>(adapterWfNotifyInfo.getTimestamp());
	};
}
