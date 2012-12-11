package de.scoopgmbh.copper.gui.util;

import java.util.ResourceBundle;

public class MessageProvider {
	private ResourceBundle bundle;
	
	public MessageProvider(ResourceBundle bundle) {
		super();
		this.bundle = bundle;
	}
	
	public String getText(String key) {
		return bundle.getString(key);
	}

	public ResourceBundle getBundle() {
		return bundle;
	}
}
