package de.scoopgmbh.copper.monitoring.core.debug;

import java.io.Serializable;

public class Method implements Serializable {
	
	private static final long serialVersionUID = 1L;

	final String definingClass;
	final String declaration;
	
	public Method(String definingClass, String declaration) {
		this.declaration = declaration.intern();
		this.definingClass = definingClass.intern();
	}

}
