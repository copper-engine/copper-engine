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
package org.copperengine.core.persistent;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.copperengine.core.ProcessingState;
import org.copperengine.core.Workflow;
import org.copperengine.core.internal.WorkflowAccessor;
import org.copperengine.core.util.FunctionWithException;
import org.copperengine.management.model.AuditTrailInfo;
import org.copperengine.management.model.WorkflowInstanceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonSQLHelper {
    private static final Logger logger = LoggerFactory.getLogger(AbstractSqlDialect.class);

    static StringBuilder appendDates(StringBuilder sql, List<Object> params, WorkflowInstanceFilter filter) {
        if (filter.getCreationTS() != null) {
            if (filter.getCreationTS().getFrom() != null) {
                sql.append(" AND x.CREATION_TS >= ?");
                params.add(filter.getCreationTS().getFrom());
            }
            if (filter.getCreationTS().getTo() != null) {
                sql.append(" AND x.CREATION_TS < ?");
                params.add(filter.getCreationTS().getTo());
            }
        }
        if (filter.getLastModTS() != null) {
            if (filter.getLastModTS().getFrom() != null) {
                sql.append(" AND x.LAST_MOD_TS >= ?");
                params.add(filter.getLastModTS().getFrom());
            }
            if (filter.getLastModTS().getTo() != null) {
                sql.append(" AND x.LAST_MOD_TS < ?");
                params.add(filter.getLastModTS().getTo());
            }
        }

        return sql;
    }

    static StringBuilder appendStates(StringBuilder sql, List<Object> params, WorkflowInstanceFilter filter) {
        if (filter.getStates() != null && !filter.getStates().isEmpty()) {
            List<String> filterStates = new ArrayList<>();
            if (filter.getStates().contains(ProcessingState.ENQUEUED.name())) {
                filterStates.add("" + DBProcessingState.ENQUEUED.ordinal());
            }
            if (filter.getStates().contains(ProcessingState.ERROR.name())) {
                filterStates.add("" + DBProcessingState.ERROR.ordinal());
            }
            if (filter.getStates().contains(ProcessingState.WAITING.name())) {
                filterStates.add("" + DBProcessingState.WAITING.ordinal());
            }
            if (filter.getStates().contains(ProcessingState.INVALID.name())) {
                filterStates.add("" + DBProcessingState.INVALID.ordinal());
            }
            if (filter.getStates().contains(ProcessingState.FINISHED.name())) {
                filterStates.add("" + DBProcessingState.FINISHED.ordinal());
            }


            if (!filterStates.isEmpty()) {
                sql.append(" AND x.STATE in (" + String.join(", ", Collections.nCopies(filterStates.size(), "?"))  + ")");
                params.addAll(filterStates);
            }
        }
        return sql;
    }

    public static List<Workflow<?>> processResult(String sql, List<Object> params, String sqlQueryErrorData, Connection con, FunctionWithException<ResultSet, PersistentWorkflow<?>> decode) throws SQLException {
        final List<Workflow<?>> result = new ArrayList<>();
        try (PreparedStatement pStmtQueryWFIs = con.prepareStatement(sql.toString()); PreparedStatement pStmtQueryErrorData = con.prepareStatement(sqlQueryErrorData)) {
            for (int i=1; i<=params.size(); i++) {
                pStmtQueryWFIs.setObject(i, params.get(i-1));
            }
            ResultSet rs = pStmtQueryWFIs.executeQuery();
            while (rs.next()) {
                try {
                    final PersistentWorkflow<?> wf = decode.apply(rs);
                    if (wf.getProcessingState() == ProcessingState.ERROR) {
                        pStmtQueryErrorData.setString(1, wf.getId());
                        try (ResultSet rsErrorData = pStmtQueryErrorData.executeQuery()) {
                            if (rsErrorData.next()) {
                                final org.copperengine.core.persistent.ErrorData errorData = new org.copperengine.core.persistent.ErrorData();
                                errorData.setExceptionStackTrace(rsErrorData.getString("EXCEPTION"));
                                errorData.setErrorTS(rsErrorData.getTimestamp("ERROR_TS"));
                                WorkflowAccessor.setErrorData(wf, errorData);
                            }
                        }
                    }
                    result.add(wf);
                } catch (Exception e) {
                    logger.error("decoding of '" + rs.getString("ID") + "' failed: " + e.toString(), e);
                }
            }
        }
        return result;
    }

    public static List<AuditTrailInfo> processAuditResult(String sql, List<Object> params, Connection con, boolean loadMessage) throws SQLException {
        final List<AuditTrailInfo> result = new ArrayList<>();
        try (PreparedStatement pStmtQueryWFIs = con.prepareStatement(sql.toString())) {
            for (int i=1; i<=params.size(); i++) {
                pStmtQueryWFIs.setObject(i, params.get(i-1));
            }
            ResultSet rs = pStmtQueryWFIs.executeQuery();
            while (rs.next()) {
                try {
                    final AuditTrailInfo auditTrailInfo = new AuditTrailInfo(
                            rs.getLong("SEQ_ID"),
                            rs.getString("TRANSACTION_ID"),
                            rs.getString("CONVERSATION_ID"),
                            rs.getString("CORRELATION_ID"),
                            rs.getTimestamp("OCCURRENCE").getTime(),
                            rs.getInt("LOGLEVEL"),
                            rs.getString("CONTEXT"),
                            rs.getString("INSTANCE_ID"),
                            rs.getString("MESSAGE_TYPE"));
                    if (loadMessage) {
                        Clob message = rs.getClob("LONG_MESSAGE");
                        if ((int) message.length() > 0) {
                            auditTrailInfo.setMessage(message.getSubString(1, (int) message.length()));
                        }
                    }

                    result.add(auditTrailInfo);
                } catch (Exception e) {
                    logger.error("decoding of '" + rs.getString("ID") + "' failed: " + e.toString(), e);
                }
            }
        }
        return result;
    }

    public static int processCountResult(StringBuilder sql, List<Object> params, Connection con) throws SQLException {
        try (PreparedStatement pStmtQueryWFIs = con.prepareStatement(sql.toString())) {
            for (int i=1; i<=params.size(); i++) {
                pStmtQueryWFIs.setObject(i, params.get(i-1));
            }
            ResultSet rs = pStmtQueryWFIs.executeQuery();
            while (rs.next()) {
                try {
                    int workflowCount = rs.getInt("COUNT_NUMBER");
                    logger.debug("Counted " + workflowCount + " workflows");
                    return workflowCount;
                } catch (Exception e) {
                    logger.error("decoding of '" + rs + "' failed: " + e.toString(), e);
                }
            }
        }

        throw new SQLException("Failed to get result of SQL request for counting workflow instances");
    }
}
