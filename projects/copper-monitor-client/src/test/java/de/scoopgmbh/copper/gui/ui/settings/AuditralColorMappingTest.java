package de.scoopgmbh.copper.gui.ui.settings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javafx.scene.paint.Color;

import org.junit.Assert;
import org.junit.Test;


public class AuditralColorMappingTest {
	
	@Test
	public void test_Serializable() {
		AuditralColorMapping auditralColorMapping = new AuditralColorMapping();
		auditralColorMapping.color.setValue(Color.AQUA);
		auditralColorMapping.contextRegEx.setValue("42");
		byte[] data;
		
		try (ByteArrayOutputStream os=new ByteArrayOutputStream()){
			ObjectOutputStream o = new ObjectOutputStream(os);
			o.writeObject(auditralColorMapping);
			data = os.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		try (ByteArrayInputStream is=new ByteArrayInputStream(data)){
			ObjectInputStream o = new ObjectInputStream(is);
			AuditralColorMapping result= (AuditralColorMapping)o.readObject();
			Assert.assertEquals(auditralColorMapping.color.getValue().toString(), result.color.getValue().toString());
			Assert.assertEquals(auditralColorMapping.contextRegEx.getValue().toString(), result.contextRegEx.getValue().toString());
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
	}

}
