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
package org.copperengine.core.audit;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.batcher.Batcher;
import org.copperengine.core.batcher.CommandCallback;
import org.copperengine.core.db.utility.JdbcUtils;
import org.copperengine.management.AuditTrailMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fast db based audit trail implementation. It is possible to extend the COPPER audit trail with custom attributes.
 * See JUnitTest {@code BatchingAuditTrailTest.testCustomTable()} for an example.
 *
 * @author austermann
 */
public class BatchingAuditTrail implements AuditTrail, AuditTrailMXBean {

    private static final Logger logger = LoggerFactory.getLogger(BatchingAuditTrail.class);

    public static final class Property2ColumnMapping {
        String columnName;
        String propertyName;

        public Property2ColumnMapping() {
        }

        public Property2ColumnMapping(String propertyName, String columnName) {
            this.columnName = columnName;
            this.propertyName = propertyName;
        }

        public String getColumnName() {
            return columnName;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }
    }

    private Batcher batcher;
    // TODO: Move datasource to SpringTxnAudit trail, then do not explore the database type, set it as an parameter
    // (isOracle)
    private DataSource dataSource;
    private int level = 5;
    protected MessagePostProcessor messagePostProcessor = new DummyPostProcessor();
    private Class<?> auditTrailEventClass;
    private String dbTable = "COP_AUDIT_TRAIL_EVENT";
    private List<Property2ColumnMapping> mapping;

    private final List<Method> propertyGetters = new ArrayList<Method>();
    // TODO: do not explore the database type, set it as an parameter (isOracle)
    private boolean isOracle;
    private String sqlStmt;

    public BatchingAuditTrail() {
        mapping = createDefaultMapping();
        auditTrailEventClass = AuditTrailEvent.class;
    }

    public static List<Property2ColumnMapping> createDefaultMapping() {
        List<Property2ColumnMapping> mapping = new ArrayList<BatchingAuditTrail.Property2ColumnMapping>();
        mapping.add(new Property2ColumnMapping("logLevel", "LOGLEVEL"));
        mapping.add(new Property2ColumnMapping("occurrence", "OCCURRENCE"));
        mapping.add(new Property2ColumnMapping("conversationId", "CONVERSATION_ID"));
        mapping.add(new Property2ColumnMapping("context", "CONTEXT"));
        mapping.add(new Property2ColumnMapping("instanceId", "INSTANCE_ID"));
        mapping.add(new Property2ColumnMapping("correlationId", "CORRELATION_ID"));
        mapping.add(new Property2ColumnMapping("transactionId", "TRANSACTION_ID"));
        mapping.add(new Property2ColumnMapping("messageType", "MESSAGE_TYPE"));
        mapping.add(new Property2ColumnMapping("message", "LONG_MESSAGE"));
        return mapping;
    }

    public void setMessagePostProcessor(MessagePostProcessor messagePostProcessor) {
        this.messagePostProcessor = messagePostProcessor;
    }

    public void setBatcher(Batcher batcher) {
        this.batcher = batcher;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setAuditTrailEventClass(Class<?> auditTrailEventClass) {
        this.auditTrailEventClass = auditTrailEventClass;
    }

    public void setDbTable(String dbTable) {
        this.dbTable = dbTable;
    }

    public String getDbTable() {
        return dbTable;
    }

    public void setMapping(List<Property2ColumnMapping> mapping) {
        this.mapping = mapping;
    }

    public void setAdditionalMapping(List<Property2ColumnMapping> mapping) {
        ArrayList<Property2ColumnMapping> newMapping = new ArrayList<BatchingAuditTrail.Property2ColumnMapping>();
        newMapping.addAll(mapping);
        newMapping.addAll(this.mapping);
        this.mapping = newMapping;
    }

    public void startup() throws Exception {
        logger.info("Starting up...");
        final Connection con = dataSource.getConnection();
        try {
            isOracle = con.getMetaData().getDatabaseProductName().equalsIgnoreCase("oracle");
        } finally {
            JdbcUtils.closeConnection(con);
        }
        sqlStmt = createSqlStmt();
    }

    private String createSqlStmt() throws IntrospectionException {
        final BeanInfo beanInfo = Introspector.getBeanInfo(auditTrailEventClass);
        final StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(dbTable).append(" (");
        int numbOfParams = 0;
        if (isOracle) {
            sql.append("SEQ_ID");
            numbOfParams++;
        }
        for (Property2ColumnMapping entry : mapping) {
            if (numbOfParams > 0) {
                sql.append(",");
            }
            sql.append(entry.getColumnName());

            boolean found = false;
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                if (pd.getName().equals(entry.getPropertyName())) {
                    propertyGetters.add(pd.getReadMethod());
                    found = true;
                    break;
                }
            }
            if (!found)
                throw new IllegalArgumentException("Cannot find read method for property '" + entry.getPropertyName() + "' in class '" + auditTrailEventClass + "'");
            numbOfParams++;
        }
        sql.append(") VALUES (");
        if (isOracle) {
            sql.append("NVL(?,COP_SEQ_AUDIT_TRAIL.NEXTVAL),");
            numbOfParams--;
        }
        for (int i = 0; i < numbOfParams; i++) {
            if (i > 0) {
                sql.append(",");
            }
            sql.append("?");
        }
        sql.append(")");
        return sql.toString();
    }

    String getSqlStmt() {
        return sqlStmt;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public boolean isEnabled(int level) {
        return this.level >= level;
    }

    @Override
    public void synchLog(int logLevel, Date occurrence, String conversationId, String context, String instanceId, String correlationId, String transactionId, String _message, String messageType) {
        this.synchLog(new AuditTrailEvent(logLevel, occurrence, conversationId, context, instanceId, correlationId, transactionId, _message, messageType, null));

    }

    @Override
    public void asynchLog(int logLevel, Date occurrence, String conversationId, String context, String instanceId, String correlationId, String transactionId, String _message, String messageType) {
        this.asynchLog(new AuditTrailEvent(logLevel, occurrence, conversationId, context, instanceId, correlationId, transactionId, _message, messageType, null));
    }

    @Override
    public void asynchLog(int logLevel, Date occurrence, String conversationId, String context, String instanceId, String correlationId, String transactionId, String _message, String messageType, final AuditTrailCallback cb) {
        this.asynchLog(new AuditTrailEvent(logLevel, occurrence, conversationId, context, instanceId, correlationId, transactionId, _message, messageType, null), cb);
    }

    @Override
    public void asynchLog(AuditTrailEvent e) {
        doLog(e, new Acknowledge.BestEffortAcknowledge(), false);
    }

    private boolean doLog(AuditTrailEvent e, final Acknowledge ack, boolean immediate) {
        CommandCallback<BatchInsertIntoAutoTrail.Command> callback = new CommandCallback<BatchInsertIntoAutoTrail.Command>() {
            @Override
            public void commandCompleted() {
                ack.onSuccess();
            }

            @Override
            public void unhandledException(Exception e) {
                ack.onException(e);
            }
        };
        return doLog(e, immediate, callback);
    }

    private boolean doLog(AuditTrailEvent e, boolean immediate, CommandCallback<BatchInsertIntoAutoTrail.Command> callback) {
        if (isEnabled(e.logLevel)) {
            logger.debug("doLog({})", e);
            e.setMessage(messagePostProcessor.serialize(e.message));
            batcher.submitBatchCommand(createBatchCommand(e, immediate, callback));
            return true;
        }
        return false;
    }

    protected BatchInsertIntoAutoTrail.Command createBatchCommand(AuditTrailEvent e, boolean immediate,
            CommandCallback<BatchInsertIntoAutoTrail.Command> callback) {
        return new BatchInsertIntoAutoTrail.Command(e, isOracle, sqlStmt, propertyGetters, callback, immediate ? 0 : 250);
    }

    @Override
    public void asynchLog(final AuditTrailEvent e, final AuditTrailCallback cb) {
        CommandCallback<BatchInsertIntoAutoTrail.Command> callback = new CommandCallback<BatchInsertIntoAutoTrail.Command>() {
            @Override
            public void commandCompleted() {
                cb.done();
            }

            @Override
            public void unhandledException(Exception e) {
                cb.error(e);
            }
        };
        doLog(e, false, callback);
    }

    @Override
    public void synchLog(final AuditTrailEvent event) {
        Acknowledge.DefaultAcknowledge ack = new Acknowledge.DefaultAcknowledge();
        if (doLog(event, ack, true)) {
            ack.waitForAcknowledge();
        }
    }

    protected DataSource getDataSource() {
        return dataSource;
    }

}
