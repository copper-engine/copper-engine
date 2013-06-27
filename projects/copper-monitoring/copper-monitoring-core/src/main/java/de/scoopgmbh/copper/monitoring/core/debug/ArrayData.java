package de.scoopgmbh.copper.monitoring.core.debug;

public class ArrayData extends Data {

	private static final long serialVersionUID = 1L;
	
	final Data[] data;

	public ArrayData(String type, int objectId, Data[] data) {
		super(type, objectId);
		this.data = data;
	}

}
