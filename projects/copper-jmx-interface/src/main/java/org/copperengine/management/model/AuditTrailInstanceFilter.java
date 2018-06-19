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

package org.copperengine.management.model;

import java.io.Serializable;

public class AuditTrailInstanceFilter implements Serializable {

    private static final long serialVersionUID = 1220517848783719579L;

    private String transactionId;
    private String conversationId;
    private String correlationId;
    private Integer level = null;
    private int max;
    private int offset;
    private boolean includeMessages = true;

    public AuditTrailInstanceFilter() {}

    public AuditTrailInstanceFilter(String transactionId, String conversationId, String correlationId, Integer level, int max, int offset) {
        this.transactionId = transactionId;
        this.conversationId = conversationId;
        this.correlationId = correlationId;
        this.level = level;
        this.max = max;
        this.offset = offset;
    }

    public AuditTrailInstanceFilter(String transactionId, String conversationId, String correlationId, Integer level, int max, int offset, boolean includeMessages) {
        this.transactionId = transactionId;
        this.conversationId = conversationId;
        this.correlationId = correlationId;
        this.level = level;
        this.max = max;
        this.offset = offset;
        this.includeMessages = includeMessages;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public boolean isIncludeMessages() {
        return includeMessages;
    }

    public void setIncludeMessages(boolean includeMessages) {
        this.includeMessages = includeMessages;
    }

    @Override
    public String toString() {
        return "AuditTrailInstanceFilter{" +
                "transactionId='" + transactionId + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", level=" + level +
                ", max=" + max +
                ", offset=" + offset +
                ", includeMessages=" + includeMessages +
                '}';
    }
}
