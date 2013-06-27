package de.scoopgmbh.copper.monitoring.core.debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StructuredData extends Data  {
	
	public StructuredData(String type, int objectId) {
		super(type, objectId);
		member = new ArrayList<Member>();
	}
	private static final long serialVersionUID = 1L;

	public List<Member> member;
	
	public void addMember(Member m) {
		member.add(m);
	}

	@Override
	public String getDisplayValue() {
		return type+" (id="+objectId+")";
	}
	
	@Override
	public Collection<? extends DisplayableNode> getChildren() {
		return member;
	}

}
