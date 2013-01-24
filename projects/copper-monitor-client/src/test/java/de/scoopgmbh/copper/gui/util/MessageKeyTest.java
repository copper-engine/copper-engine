package de.scoopgmbh.copper.gui.util;

import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Test;


public class MessageKeyTest {
	
	@Test
	public void testKeyInProperty(){
		MessageProvider messageProvider = new MessageProvider(ResourceBundle.getBundle("de.scoopgmbh.copper.gui.message"));
		for (MessageKey key: MessageKey.values()){
			Assert.assertNotNull(messageProvider.getText(key)); 
		}
	}

}
