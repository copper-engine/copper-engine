package de.scoopgmbh.copper.monitoring.core.debug;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

public abstract class Data implements Serializable, DisplayableNode {
	
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
	
	@Override
	public abstract String getDisplayValue(); 
	
	@Override
	public Collection<? extends DisplayableNode> getChildren() {
		return Collections.emptyList();
	}
	
	@Override
	public NodeTyp getTyp() {
		return null;
	}
}
