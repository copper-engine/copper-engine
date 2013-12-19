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
package de.scoopgmbh.copper.monitoring.client.ui.settings;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SettingsModel implements Serializable {
    private static final long serialVersionUID = -2305027466935186248L;
    public ObservableList<AuditralColorMapping> auditralColorMappings = FXCollections.observableList(new ArrayList<AuditralColorMapping>());
    public SimpleStringProperty lastConnectedServer = new SimpleStringProperty("");

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(lastConnectedServer.get());
        out.writeInt(auditralColorMappings.size());
        for (int i = 0; i < auditralColorMappings.size(); i++) {
            out.writeObject(auditralColorMappings.get(i));
        }
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        lastConnectedServer = new SimpleStringProperty((String) in.readObject());
        auditralColorMappings = FXCollections.observableList(new ArrayList<AuditralColorMapping>());
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            auditralColorMappings.add((AuditralColorMapping) in.readObject());
        }
    }

}
