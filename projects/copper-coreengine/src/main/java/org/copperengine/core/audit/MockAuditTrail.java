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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock implementation of an audit trail.
 *
 * @author austermann
 */
public class MockAuditTrail implements AuditTrail {

    private static final Logger logger = LoggerFactory.getLogger(MockAuditTrail.class);

    private int level = 5;

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public boolean isEnabled(int level) {
        return this.level >= level;
    }

    @Override
    public void synchLog(int logLevel, Date occurrence, String conversationId, String context, String instanceId, String correlationId, String transactionId, String message, String messageType) {
        if (isEnabled(logLevel))
            logger.info(createMessage(logLevel, occurrence, conversationId, context, instanceId, correlationId, transactionId, message, messageType));
    }

    public void asynchLog(int logLevel, Date occurrence, String conversationId, String context, String instanceId, String correlationId, String transactionId, String message, String messageType) {
        if (isEnabled(logLevel))
            logger.info(createMessage(logLevel, occurrence, conversationId, context, instanceId, correlationId, transactionId, message, messageType));
    }

    public void asynchLog(int logLevel, Date occurrence, String conversationId, String context, String instanceId, String correlationId, String transactionId, String message, String messageType, AuditTrailCallback cb) {
        if (isEnabled(logLevel))
            logger.info(createMessage(logLevel, occurrence, conversationId, context, instanceId, correlationId, transactionId, message, messageType));
        cb.done();
    }

    private String createMessage(int logLevel, Date occurrence, String conversationId, String context, String instanceId, String correlationId, String transactionId, String message, String messageType) {
        return new StringBuilder()
                .append(logLevel).append('|')
                .append(occurrence).append('|')
                .append(conversationId).append('|')
                .append(context).append('|')
                .append(instanceId).append('|')
                .append(correlationId).append('|')
                .append(transactionId).append('|')
                .append(message).append("|")
                .append(messageType)
                .toString();
    }

    @Override
    public int getLevel() {
        return level;
    }

    public void asynchLog(AuditTrailEvent e) {
        this.asynchLog(e.logLevel, e.occurrence, e.conversationId, e.context, e.instanceId, e.correlationId, e.transactionId, e.message, e.messageType);
    }

    public void asynchLog(AuditTrailEvent e, AuditTrailCallback cb) {
        this.asynchLog(e.logLevel, e.occurrence, e.conversationId, e.context, e.instanceId, e.correlationId, e.transactionId, e.message, e.messageType, cb);
    }

    @Override
    public void synchLog(AuditTrailEvent e) {
        this.synchLog(e.logLevel, e.occurrence, e.conversationId, e.context, e.instanceId, e.correlationId, e.transactionId, e.message, e.messageType);
    }

}
