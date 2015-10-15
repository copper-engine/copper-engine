/*
 * Copyright 2002-2015 SCOOP Software GmbH
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
package org.copperengine.core.audit;

import java.io.Serializable;
import java.util.Date;

public class AuditTrailEvent implements Serializable {

    private static final long serialVersionUID = -6195270904233529021L;

    protected int logLevel;
    protected Date occurrence;
    protected String conversationId;
    protected String context;
    protected String instanceId;
    protected String correlationId;
    protected String message;
    protected String transactionId;
    protected String messageType;
    protected Long sequenceId;

    public AuditTrailEvent(int logLevel, Date occurrence, String conversationId, String context, String instanceId, String correlationId, String transactionId, String message, String messageType) {
        this(logLevel, occurrence, conversationId, context, instanceId, correlationId, transactionId, message, messageType, null);
    }

    public AuditTrailEvent(int logLevel, Date occurrence, String conversationId, String context, String instanceId, String correlationId, String transactionId, String message, String messageType, Long sequenceId) {
        super();
        if (occurrence == null)
            throw new IllegalArgumentException("occurence is null");
        if (conversationId == null)
            throw new IllegalArgumentException("conversationId is null");
        if (context == null)
            throw new IllegalArgumentException("context is null");
        if (conversationId.length() > 64)
            throw new IllegalArgumentException("conversationId is too long (>64)");
        if (context.length() > 128)
            throw new IllegalArgumentException("context is too long (>128)");
        if (instanceId != null && instanceId.length() > 128)
            throw new IllegalArgumentException("instanceId is too long (>128)");
        if (correlationId != null && correlationId.length() > 128)
            throw new IllegalArgumentException("correlationId is too long (>128)");
        if (transactionId != null && transactionId.length() > 128)
            throw new IllegalArgumentException("transactionId is too long (>128)");
        if (messageType != null && messageType.length() > 256)
            throw new IllegalArgumentException("messageType is too long (>256)");
        if (logLevel < 0 || logLevel > 99)
            throw new IllegalArgumentException("logLevel must bee between 0 and 99");
        this.sequenceId = sequenceId;
        this.logLevel = logLevel;
        this.occurrence = occurrence;
        this.conversationId = conversationId;
        this.context = context;
        this.instanceId = instanceId;
        this.correlationId = correlationId;
        this.message = message;
        this.transactionId = transactionId;
        this.messageType = messageType;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public Date getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(Date occurrence) {
        this.occurrence = occurrence;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
    }

    @Override
    public String toString() {
        return "AuditTrailEvent [logLevel=" + logLevel + ", occurrence="
                + occurrence + ", conversationId=" + conversationId
                + ", context=" + context + ", instanceId=" + instanceId
                + ", correlationId=" + correlationId + ", message=" + message
                + ", transactionId=" + transactionId + ", messageType="
                + messageType + ", sequenceId=" + sequenceId + "]";
    }

}
