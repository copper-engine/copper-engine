package de.scoopgmbh.copper.monitoring.core.debug;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;


public class Member implements Serializable, DisplayableNode {
	private static final long serialVersionUID = 1L;

	final String name;
	final String declaredType;
	final Data   value;
	
	public Member(String name, String declaredType, Data value) {
		this.name = name.intern();
		this.value = value;
		this.declaredType = declaredType.intern();
	}
	
	public String getName() {
		return name;
	}

	public Data getValue() {
		return value;
	}
	
	@Override
	public Collection<DisplayableNode> getChildren() {
		return Arrays.<DisplayableNode>asList(value);
	}
	
	@Override
	public String getDisplayValue() {
		return name+" : "+declaredType;
	}
	
	@Override
	public NodeTyp getTyp() {
		return null;
	}

}
