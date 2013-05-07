package de.scoopgmbh.copper.monitoring.core.model;

import java.io.Serializable;
import java.util.Date;

public class AdapterEventBase implements Serializable{
	private static final long serialVersionUID = 6386090675365126626L;
	
	String adapterName;
	Date timestamp;

	public String getAdapterName() {
		return adapterName;
	}

	public void setAdapterName(String adapterName) {
		this.adapterName = adapterName;
	}

	public AdapterEventBase(String adapterName, Date timestamp) {
		super();
		this.adapterName = adapterName;
		this.timestamp = timestamp;
	}

	public AdapterEventBase() {
		super();
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	
}