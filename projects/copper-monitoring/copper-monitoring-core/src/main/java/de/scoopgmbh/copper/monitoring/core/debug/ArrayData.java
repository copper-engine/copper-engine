package de.scoopgmbh.copper.monitoring.core.debug;

import java.util.Arrays;
import java.util.Collection;

public class ArrayData extends Data {

	private static final long serialVersionUID = 1L;
	
	final Member[] data;

	public ArrayData(Class<?> arrayType, int objectId, Data[] data) {
		super(arrayType.getCanonicalName(), objectId);
		this.data = new Member[data.length];
		for (int i = 0; i < data.length; ++i) {
			this.data[i] = new Member(""+i, arrayType.getComponentType().getCanonicalName(), data[i] != null?data[i]:Data.NULL);
		}
	}
	
	@Override
	public Collection<DisplayableNode> getChildren() {
		return Arrays.<DisplayableNode>asList(data);
	}

	@Override
	public String getDisplayValue() {
		return type;
	}

}
