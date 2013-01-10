package de.scoopgmbh.copper.gui.ui.audittrail.result;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import de.scoopgmbh.copper.monitor.adapter.model.AuditTrailInfo;

public class AuditTrailResultModel {

	public SimpleLongProperty id;
	public SimpleStringProperty occurrence;
	public SimpleStringProperty conversationId;
	public SimpleIntegerProperty loglevel;
	public SimpleStringProperty context;
	public SimpleStringProperty workflowInstanceId;
	public SimpleStringProperty correlationId;
	public SimpleStringProperty transactionId;
	public SimpleStringProperty messageType;
	

	public AuditTrailResultModel(AuditTrailInfo auditTrailInfo) {
		id= new SimpleLongProperty(auditTrailInfo.getId());
		occurrence = new SimpleStringProperty(auditTrailInfo.getOccurrence()!=null?auditTrailInfo.getOccurrence().toString():"");
		conversationId = new SimpleStringProperty(auditTrailInfo.getConversationId());
		loglevel= new SimpleIntegerProperty(auditTrailInfo.getLoglevel());
		context = new SimpleStringProperty(auditTrailInfo.getContext());
		workflowInstanceId = new SimpleStringProperty(auditTrailInfo.getWorkflowInstanceId());
		correlationId = new SimpleStringProperty(auditTrailInfo.getCorrelationId());
		transactionId = new SimpleStringProperty(auditTrailInfo.getTransactionId());
		messageType = new SimpleStringProperty(auditTrailInfo.getMessageType());
	}
	
	
	
	
}
