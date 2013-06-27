package de.scoopgmbh.copper.monitoring.core.debug;

import java.io.Serializable;

public abstract class Data implements Serializable {
	
	private static final long serialVersionUID = 1L;

	final String type;
	public int objectId;

	public Data(String type, int objectId) {
		this.type = type.intern();
		this.objectId = objectId;
	}
	
	public String getType() {
		return type;
	}
}
