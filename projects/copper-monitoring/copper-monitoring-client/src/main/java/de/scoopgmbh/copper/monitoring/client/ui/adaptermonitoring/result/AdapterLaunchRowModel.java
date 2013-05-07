package de.scoopgmbh.copper.monitoring.client.ui.adaptermonitoring.result;

import java.util.Date;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import de.scoopgmbh.copper.monitoring.core.model.AdapterWfLaunchInfo;

public class AdapterLaunchRowModel {
	public final SimpleStringProperty workflowname;
	public final SimpleObjectProperty<Date> timestamp;

	public AdapterLaunchRowModel(AdapterWfLaunchInfo  adapterWfLaunchInfo){
		workflowname= new SimpleStringProperty(adapterWfLaunchInfo.getWorkflowname());
		timestamp= new SimpleObjectProperty<Date>(adapterWfLaunchInfo.getTimestamp());
	};
}
