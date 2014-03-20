/*
 * Copyright 2002-2014 SCOOP Software GmbH
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SettingsModel implements Serializable {
    private static final long serialVersionUID = 2;
    public ObservableList<AuditralColorMapping> auditralColorMappings = FXCollections.observableList(new ArrayList<AuditralColorMapping>());
    public SimpleStringProperty lastConnectedServer = new SimpleStringProperty("");
    public SimpleStringProperty cssUri = new SimpleStringProperty("");
    public static final String SETTINGS_KEY = "settings";

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(lastConnectedServer.get());
        out.writeInt(auditralColorMappings.size());
        for (AuditralColorMapping auditralColorMapping : auditralColorMappings) {
            out.writeObject(auditralColorMapping);
        }
        out.writeObject(cssUri.get());
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        lastConnectedServer = new SimpleStringProperty((String) in.readObject());
        auditralColorMappings = FXCollections.observableList(new ArrayList<AuditralColorMapping>());
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            auditralColorMappings.add((AuditralColorMapping) in.readObject());
        }
        cssUri = new SimpleStringProperty((String) in.readObject());
    }

    public static SettingsModel from(final Preferences prefs, byte[] defaultModelBytes) throws Exception {
        ByteArrayInputStream is = null;
        try {
            is = new ByteArrayInputStream(prefs.getByteArray(SETTINGS_KEY, defaultModelBytes));
            ObjectInputStream o = new ObjectInputStream(is);
            Object object = o.readObject();
            if (object instanceof SettingsModel) {
                SettingsModel settingsModel = (SettingsModel) object;
                return settingsModel;
            } else {
                throw new Exception("Not a SettingsModel: " + object);
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    
    public void saveSettings(final Preferences prefs) {
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(os);
            o.writeObject(this);
            prefs.putByteArray(SettingsModel.SETTINGS_KEY, os.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
