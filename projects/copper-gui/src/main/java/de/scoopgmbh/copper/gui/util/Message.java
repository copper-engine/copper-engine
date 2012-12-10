package de.scoopgmbh.copper.gui.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Message {
	private static ResourceBundle bundle;

	public static String getText(String key) {

		try {
			if (bundle==null){
				bundle = ResourceBundle.getBundle("de.scoopgmbh.copper.gui.message");
			}
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			throw new RuntimeException(e);
		}

	}
}
