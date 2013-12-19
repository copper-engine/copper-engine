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
package de.scoopgmbh.copper.monitoring.client.ui.audittrail.result;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import de.scoopgmbh.copper.monitoring.core.model.AuditTrailInfo;

public class AuditTrailResultModel {

    public final SimpleLongProperty id;
    public final SimpleStringProperty occurrence;
    public final SimpleStringProperty conversationId;
    public final SimpleIntegerProperty loglevel;
    public final SimpleStringProperty context;
    public final SimpleStringProperty workflowInstanceId;
    public final SimpleStringProperty correlationId;
    public final SimpleStringProperty transactionId;
    public final SimpleStringProperty messageType;

    public AuditTrailResultModel(AuditTrailInfo auditTrailInfo) {
        id = new SimpleLongProperty(auditTrailInfo.getId());
        occurrence = new SimpleStringProperty(auditTrailInfo.getOccurrence() != null ? auditTrailInfo.getOccurrence().toString() : "");
        conversationId = new SimpleStringProperty(auditTrailInfo.getConversationId());
        loglevel = new SimpleIntegerProperty(auditTrailInfo.getLoglevel());
        context = new SimpleStringProperty(auditTrailInfo.getContext());
        workflowInstanceId = new SimpleStringProperty(auditTrailInfo.getWorkflowInstanceId());
        correlationId = new SimpleStringProperty(auditTrailInfo.getCorrelationId());
        transactionId = new SimpleStringProperty(auditTrailInfo.getTransactionId());
        messageType = new SimpleStringProperty(auditTrailInfo.getMessageType());
    }

}
