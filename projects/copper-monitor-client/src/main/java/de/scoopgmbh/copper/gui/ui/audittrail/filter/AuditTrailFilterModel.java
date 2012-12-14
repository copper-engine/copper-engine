package de.scoopgmbh.copper.gui.ui.audittrail.filter;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class AuditTrailFilterModel {
	public SimpleStringProperty workflowClass = new SimpleStringProperty();
	public SimpleStringProperty workflowInstanceId = new SimpleStringProperty();
	public SimpleStringProperty correlationId = new SimpleStringProperty();
	public SimpleIntegerProperty level = new SimpleIntegerProperty();
}
