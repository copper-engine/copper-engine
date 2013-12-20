/*
 * Copyright 2002-2013 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.copperengine.monitoring.client.ui.settings;

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

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ObjectOutputStream o = new ObjectOutputStream(os);
            o.writeObject(settingsModel);
            data = os.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        ByteArrayInputStream is = new ByteArrayInputStream(data);
        try {
            ObjectInputStream o = new ObjectInputStream(is);
            SettingsModel result = (SettingsModel) o.readObject();
            Assert.assertEquals(settingsModel.auditralColorMappings.size(), result.auditralColorMappings.size());
            Assert.assertEquals(settingsModel.auditralColorMappings.get(1).contextRegEx.getValue(), settingsModel.auditralColorMappings.get(1).contextRegEx.getValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
