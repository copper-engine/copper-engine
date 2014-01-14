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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;

import org.copperengine.monitoring.client.ui.audittrail.result.AuditTrailResultModel;

public class AuditralColorMapping implements Serializable {
    private static final long serialVersionUID = 2623655111522368358L;

    public SimpleStringProperty idRegEx = new SimpleStringProperty("");
    public SimpleStringProperty occurrenceRegEx = new SimpleStringProperty("");
    public SimpleStringProperty conversationIdRegEx = new SimpleStringProperty("");
    public SimpleStringProperty loglevelRegEx = new SimpleStringProperty("");
    public SimpleStringProperty contextRegEx = new SimpleStringProperty("");
    public SimpleStringProperty workflowInstanceIdRegEx = new SimpleStringProperty("");
    public SimpleStringProperty correlationIdRegEx = new SimpleStringProperty("");
    public SimpleStringProperty transactionIdRegEx = new SimpleStringProperty("");
    public SimpleStringProperty messageTypeRegEx = new SimpleStringProperty("");
    public SimpleObjectProperty<Color> color = new SimpleObjectProperty<Color>();

    public boolean match(AuditTrailResultModel item) {
        return (!isEmpty(item.id.getValue()) && ("" + item.id.getValue()).matches(idRegEx.getValue())) ||
                (!isEmpty(item.occurrence.getValue()) && (item.occurrence.getValue()).matches(occurrenceRegEx.getValue())) ||
                (!isEmpty(item.conversationId.getValue()) && (item.conversationId.getValue()).matches(conversationIdRegEx.getValue())) ||
                (!isEmpty(item.loglevel.getValue()) && ("" + item.loglevel.getValue()).matches(loglevelRegEx.getValue())) ||
                (!isEmpty(item.context.getValue()) && (item.context.getValue()).matches(contextRegEx.getValue())) ||
                (!isEmpty(item.workflowInstanceId.getValue()) && (item.workflowInstanceId.getValue()).matches(workflowInstanceIdRegEx.getValue())) ||
                (!isEmpty(item.correlationId.getValue()) && (item.correlationId.getValue()).matches(correlationIdRegEx.getValue())) ||
                (!isEmpty(item.transactionId.getValue()) && (item.transactionId.getValue()).matches(transactionIdRegEx.getValue())) ||
                (!isEmpty(item.messageType.getValue()) && (item.messageType.getValue()).matches(messageTypeRegEx.getValue()));
    }

    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    private boolean isEmpty(Integer value) {
        return value == null;
    }

    private boolean isEmpty(Long value) {
        return value == null;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(idRegEx.getValue());
        out.writeObject(occurrenceRegEx.getValue());
        out.writeObject(conversationIdRegEx.getValue());
        out.writeObject(loglevelRegEx.getValue());
        out.writeObject(contextRegEx.getValue());
        out.writeObject(workflowInstanceIdRegEx.getValue());
        out.writeObject(correlationIdRegEx.getValue());
        out.writeObject(transactionIdRegEx.getValue());
        out.writeObject(messageTypeRegEx.getValue());
        out.writeDouble(color.getValue().getRed());
        out.writeDouble(color.getValue().getGreen());
        out.writeDouble(color.getValue().getBlue());
        out.writeDouble(color.getValue().getOpacity());
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        idRegEx = new SimpleStringProperty((String) in.readObject());
        occurrenceRegEx = new SimpleStringProperty((String) in.readObject());
        conversationIdRegEx = new SimpleStringProperty((String) in.readObject());
        loglevelRegEx = new SimpleStringProperty((String) in.readObject());
        contextRegEx = new SimpleStringProperty((String) in.readObject());
        workflowInstanceIdRegEx = new SimpleStringProperty((String) in.readObject());
        correlationIdRegEx = new SimpleStringProperty((String) in.readObject());
        transactionIdRegEx = new SimpleStringProperty((String) in.readObject());
        messageTypeRegEx = new SimpleStringProperty((String) in.readObject());
        double r = in.readDouble();
        double g = in.readDouble();
        double b = in.readDouble();
        double o = in.readDouble();
        color = new SimpleObjectProperty<Color>(new Color(r, g, b, o));
    }

}
