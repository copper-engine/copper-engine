package de.scoopgmbh.copper.monitoring.core.debug;

import java.util.Collection;

public interface DisplayableNode {
	
	String getDisplayValue();
	
	Collection<? extends DisplayableNode> getChildren();
	
	NodeTyp getTyp();

}
