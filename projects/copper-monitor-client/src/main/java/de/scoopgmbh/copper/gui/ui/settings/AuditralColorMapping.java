package de.scoopgmbh.copper.gui.ui.settings;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;
import de.scoopgmbh.copper.gui.ui.audittrail.result.AuditTrailResultModel;

public class AuditralColorMapping {

	public final SimpleStringProperty idRegEx = new SimpleStringProperty("");
	public final SimpleStringProperty occurrenceRegEx = new SimpleStringProperty("");
	public final SimpleStringProperty conversationIdRegEx = new SimpleStringProperty("");
	public final SimpleStringProperty loglevelRegEx = new SimpleStringProperty("");
	public final SimpleStringProperty contextRegEx = new SimpleStringProperty("");
	public final SimpleStringProperty workflowInstanceIdRegEx = new SimpleStringProperty("");
	public final SimpleStringProperty correlationIdRegEx = new SimpleStringProperty("");
	public final SimpleStringProperty transactionIdRegEx = new SimpleStringProperty("");
	public final SimpleStringProperty messageTypeRegEx = new SimpleStringProperty("");
	
	public SimpleObjectProperty<Color> color = new SimpleObjectProperty<>();

	public boolean match(AuditTrailResultModel item) {
		return (item.id.getValue()!=null && (""+item.id.getValue()).matches(idRegEx.getValue())) ||
			   (item.occurrence.getValue()!=null && (item.occurrence.getValue()).matches(occurrenceRegEx.getValue())) ||
			   (item.conversationId.getValue()!=null && (item.conversationId.getValue()).matches(conversationIdRegEx.getValue())) ||
			   (item.loglevel.getValue()!=null && (""+item.loglevel.getValue()).matches(loglevelRegEx.getValue())) ||
			   (item.context.getValue()!=null && (item.context.getValue()).matches(contextRegEx.getValue())) ||
			   (item.workflowInstanceId.getValue()!=null && (item.workflowInstanceId.getValue()).matches(workflowInstanceIdRegEx.getValue())) ||
			   (item.correlationId.getValue()!=null && (item.correlationId.getValue()).matches(correlationIdRegEx.getValue())) ||
			   (item.transactionId.getValue()!=null && (item.transactionId.getValue()).matches(transactionIdRegEx.getValue())) ||
			   (item.messageType.getValue()!=null && (item.messageType.getValue()).matches(messageTypeRegEx.getValue()));
	}  
	
	public static void main(String[] args) {

	}
	
}
