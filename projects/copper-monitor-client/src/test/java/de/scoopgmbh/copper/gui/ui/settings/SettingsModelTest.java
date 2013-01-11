package de.scoopgmbh.copper.gui.ui.settings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javafx.scene.paint.Color;

import org.junit.Assert;
import org.junit.Test;


public class SettingsModelTest {
	@Test
	public void test_Serializable() {
		SettingsModel settingsModel = new SettingsModel();
		AuditralColorMapping auditralColorMapping = new AuditralColorMapping();
		auditralColorMapping.color.setValue(Color.AQUA);
		auditralColorMapping.contextRegEx.setValue("42");
		settingsModel.auditralColorMappings.add(auditralColorMapping);
		AuditralColorMapping auditralColorMapping2 = new AuditralColorMapping();
		auditralColorMapping2.color.setValue(Color.AQUA);
		auditralColorMapping2.contextRegEx.setValue("43");
		settingsModel.auditralColorMappings.add(auditralColorMapping2);
		
		byte[] data;
		
		try (ByteArrayOutputStream os=new ByteArrayOutputStream()){
			ObjectOutputStream o = new ObjectOutputStream(os);
			o.writeObject(settingsModel);
			data = os.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		try (ByteArrayInputStream is=new ByteArrayInputStream(data)){
			ObjectInputStream o = new ObjectInputStream(is);
			SettingsModel result= (SettingsModel)o.readObject();
			Assert.assertEquals(settingsModel.auditralColorMappings.size(), result.auditralColorMappings.size());
			Assert.assertEquals(settingsModel.auditralColorMappings.get(1).contextRegEx.getValue(), settingsModel.auditralColorMappings.get(1).contextRegEx.getValue());
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		
	}
}
