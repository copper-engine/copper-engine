/*
 * Copyright 2002-2018 SCOOP Software GmbH
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

import org.copperengine.core.persistent.ScottyDBStorageInterface;
import org.copperengine.management.AuditTrailQueryMXBean;
import org.copperengine.management.model.AuditTrailInfo;
import org.copperengine.management.model.AuditTrailInstanceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ScottyAuditTrailQueryEngine implements AuditTrailQueryMXBean {
    private static final Logger logger = LoggerFactory.getLogger(ScottyAuditTrailQueryEngine.class);

    private MessagePostProcessor messagePostProcessor;
    private ScottyDBStorageInterface dbStorage;

    @Override
    public List<AuditTrailInfo> getAuditTrails(String transactionId, String conversationId, String correlationId, Integer level, int maxResult) {
        return getAuditTrails(new AuditTrailInstanceFilter(null, transactionId, conversationId, correlationId, level, maxResult, 0, null, null, false));
    }

    @Override
    public List<AuditTrailInfo> getAuditTrails(AuditTrailInstanceFilter filter) {
        logger.info("getAuditTrails is called with filter: {}", filter);
        if (filter.isIncludeMessages() && messagePostProcessor == null) {
            throw new RuntimeException("Message Post Processor should be set to decode message");
        }

        try {
            List<AuditTrailInfo> auditTrailInfoList = dbStorage.queryAuditTrailInstances(filter);
            logger.info("getAuditTrails returned " + auditTrailInfoList.size() + " instance(s)");
            if (filter.isIncludeMessages()) {
                try {
                    auditTrailInfoList.forEach(auditTrailInfo -> {
                        auditTrailInfo.setMessage(messagePostProcessor.deserialize(auditTrailInfo.getMessage()));
                    });
                } catch(Exception e) {
                    logger.info("Failed to deserialize Audit Trail Message", e);
                }

            }

            return auditTrailInfoList;
        } catch (Exception e) {
            logger.error("getAuditTrails failed: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public int countAuditTrails(AuditTrailInstanceFilter filter) {
        try {
            int count = dbStorage.countAuditTrailInstances(filter);
            logger.debug("countAuditTrails returned {}", count);

            return count;
        } catch (Exception e) {
            logger.error("countAuditTrails failed: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getMessage(long id) {
        return getMessageString(id).getBytes();
    }

    @Override
    public String getMessageString(long id) {
        if (messagePostProcessor == null) {
            throw new RuntimeException("Message Post Processor should be set to decode message");
        }

        try {
            String message = dbStorage.queryAuditTrailMessage(id);
            return message == null ? null : messagePostProcessor.deserialize(message);
        } catch (Exception e) {
            logger.error("getMessageString for id: " + id + " failed: " + e.getMessage() , e);
            throw new RuntimeException(e);
        }
    }

    public MessagePostProcessor getMessagePostProcessor() {
        return messagePostProcessor;
    }

    public void setMessagePostProcessor(MessagePostProcessor messagePostProcessor) {
        this.messagePostProcessor = messagePostProcessor;
    }

    public ScottyDBStorageInterface getDbStorage() {
        return dbStorage;
    }

    public void setDbStorage(ScottyDBStorageInterface dbStorage) {
        this.dbStorage = dbStorage;
    }
}
