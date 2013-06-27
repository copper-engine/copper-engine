package de.scoopgmbh.copper.monitoring.core.debug;

public class PlainData extends Data {
	
	private static final long serialVersionUID = 1L;

	public PlainData(String type, int objectId, Object data) {
		super(type, objectId);
		this.value = data;
	}

	final Object value;

	@Override
	public String getDisplayValue() {
		return value != null?value.toString():"<null>";
	}

}
