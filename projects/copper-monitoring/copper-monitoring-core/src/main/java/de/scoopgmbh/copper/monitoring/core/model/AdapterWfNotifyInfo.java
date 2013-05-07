package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;
import java.util.Date;

public class AdapterWfNotifyInfo extends AdapterEventBase implements Serializable{
	private static final long serialVersionUID = -7005919571274984644L;
	
	String correlationId;
	String message;
	
	public AdapterWfNotifyInfo(String correlationId, String message, Date timestamp,String adapterName) {
		super(adapterName,timestamp);
		this.correlationId = correlationId;
		this.message = message;
	}

	public AdapterWfNotifyInfo() {
		super();
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	
	
}
