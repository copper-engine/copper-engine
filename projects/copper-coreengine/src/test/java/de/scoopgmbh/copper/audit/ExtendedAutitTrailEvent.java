package de.scoopgmbh.copper.audit;

import java.sql.Timestamp;
import java.util.Date;

public class ExtendedAutitTrailEvent extends AuditTrailEvent {

	private static final long serialVersionUID = 1L;
	
	private String customVarchar;
	private Integer customInt;
	private Timestamp customTimestamp;

	public ExtendedAutitTrailEvent(int logLevel, Date occurrence,
			String conversationId, String context, String instanceId,
			String correlationId, String transactionId, String message,
			String messageType, String customVarchar, Integer customInt,
			Timestamp customTimestamp) {
		super(logLevel, occurrence, conversationId, context, instanceId,
				correlationId, transactionId, message, messageType);
		this.customVarchar = customVarchar;
		this.customInt = customInt;
		this.customTimestamp = customTimestamp;
	}

	public String getCustomVarchar() {
		return customVarchar;
	}

	public void setCustomVarchar(String customVarchar) {
		this.customVarchar = customVarchar;
	}

	public Integer getCustomInt() {
		return customInt;
	}

	public void setCustomInt(Integer customInt) {
		this.customInt = customInt;
	}

	public Timestamp getCustomTimestamp() {
		return customTimestamp;
	}

	public void setCustomTimestamp(Timestamp customTimestamp) {
		this.customTimestamp = customTimestamp;
	}
	
	

}
