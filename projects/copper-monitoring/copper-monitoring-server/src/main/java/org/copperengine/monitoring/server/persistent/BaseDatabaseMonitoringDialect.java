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
package org.copperengine.monitoring.server.persistent;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.copperengine.core.Response;
import org.copperengine.core.audit.BatchingAuditTrail;
import org.copperengine.core.audit.MessagePostProcessor;
import org.copperengine.core.db.utility.JdbcUtils;
import org.copperengine.core.persistent.Serializer;
import org.copperengine.monitoring.core.model.AuditTrailInfo;
import org.copperengine.monitoring.core.model.MessageInfo;
import org.copperengine.monitoring.core.model.WorkflowClassMetaData;
import org.copperengine.monitoring.core.model.WorkflowInstanceInfo;
import org.copperengine.monitoring.core.model.WorkflowInstanceState;
import org.copperengine.monitoring.core.model.WorkflowStateSummary;
import org.copperengine.monitoring.core.model.WorkflowSummary;
import org.copperengine.monitoring.server.workaround.DBProcessingStateWorkaround;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation of the {@link DatabaseMonitoringDialect} for SQL databases
 */
public abstract class BaseDatabaseMonitoringDialect implements DatabaseMonitoringDialect {

    private final MessagePostProcessor messagePostProcessor;
    private static final Logger logger = LoggerFactory.getLogger(BaseDatabaseMonitoringDialect.class);
    Serializer serializer;
    private final BatchingAuditTrail batchingAuditTrail;

    public BaseDatabaseMonitoringDialect(Serializer serializer, MessagePostProcessor messagePostProcessor, BatchingAuditTrail batchingAuditTrail) {
        super();
        this.serializer = serializer;
        this.messagePostProcessor = messagePostProcessor;
        this.batchingAuditTrail = batchingAuditTrail;
    }

    @Override
    public WorkflowStateSummary selectTotalWorkflowStateSummary(Connection con) {
        PreparedStatement selectStmt = null;
        try {
            selectStmt = con.prepareStatement("select state, count(*) from COP_WORKFLOW_INSTANCE group by state");
            ResultSet result = selectStmt.executeQuery();
            selectStmt.setFetchSize(10);

            Map<WorkflowInstanceState, Integer> numberOfWorkflowInstancesWithState = new HashMap<WorkflowInstanceState, Integer>();
            while (result.next()) {
                int status = result.getInt(1);
                int count = result.getInt(2);
                numberOfWorkflowInstancesWithState.put(DBProcessingStateWorkaround.fromKey(status).asWorkflowInstanceState(), count);
            }
            result.close();
            return new WorkflowStateSummary(numberOfWorkflowInstancesWithState);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            org.copperengine.core.db.utility.JdbcUtils.closeStatement(selectStmt);
        }
    }

    @Override
    public List<AuditTrailInfo> selectAuditTrails(String workflowClass, String workflowInstanceId, String correlationId, Integer level, long resultRowLimit,
            Connection con) {

        PreparedStatement selectStmt = null;
        try {
            selectStmt = con.prepareStatement(getResultLimitingQuery(
                    "SELECT SEQ_ID,OCCURRENCE,CONVERSATION_ID,LOGLEVEL,CONTEXT,INSTANCE_ID,CORRELATION_ID,TRANSACTION_ID,MESSAGE_TYPE FROM " + batchingAuditTrail.getDbTable() + " a\n" +
                            "LEFT OUTER JOIN COP_WORKFLOW_INSTANCE i ON a.INSTANCE_ID=i.ID \n" +
                            "WHERE\n" +
                            "	(? is null or i.CLASSNAME=?) AND \n" +
                            "	(? is null or a.INSTANCE_ID=?) AND \n" +
                            "	(? is null or a.CORRELATION_ID=?) AND \n" +
                            "	(? is null or a.LOGLEVEL=?)", resultRowLimit));
            int pIdx = 1;
            pIdx = setFilterParam(selectStmt, workflowClass, java.sql.Types.VARCHAR, pIdx);
            pIdx = setFilterParam(selectStmt, workflowInstanceId, java.sql.Types.VARCHAR, pIdx);
            pIdx = setFilterParam(selectStmt, correlationId, java.sql.Types.VARCHAR, pIdx);
            pIdx = setFilterParam(selectStmt, level, java.sql.Types.INTEGER, pIdx);

            selectStmt.setFetchSize(100);

            ResultSet resultSet = selectStmt.executeQuery();

            ArrayList<AuditTrailInfo> logs = new ArrayList<AuditTrailInfo>();
            while (resultSet.next()) {
                AuditTrailInfo auditTrailInfo = new AuditTrailInfo();
                auditTrailInfo.setId(resultSet.getLong(1));
                auditTrailInfo.setOccurrence(resultSet.getTimestamp(2) != null ? new Date(resultSet.getTimestamp(2).getTime()) : null);
                auditTrailInfo.setConversationId(resultSet.getString(3));
                auditTrailInfo.setLoglevel(resultSet.getInt(4));
                auditTrailInfo.setContext(resultSet.getString(5));
                auditTrailInfo.setWorkflowInstanceId(resultSet.getString(6));
                auditTrailInfo.setCorrelationId(resultSet.getString(7));
                auditTrailInfo.setTransactionId(resultSet.getString(8));
                auditTrailInfo.setMessageType(resultSet.getString(9));
                logs.add(auditTrailInfo);
            }
            resultSet.close();

            return logs;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtils.closeStatement(selectStmt);
        }
    }

    @Override
    public String selectAuditTrailMessage(long id, Connection con) {
        PreparedStatement selectStmt = null;
        try {
            selectStmt = con.prepareStatement("SELECT LONG_MESSAGE FROM COP_AUDIT_TRAIL_EVENT WHERE SEQ_ID=?");
            selectStmt.setLong(1, id);
            ResultSet result = selectStmt.executeQuery();

            while (result.next()) {
                Clob message = result.getClob(1);
                if ((int) message.length() > 0) {
                    return messagePostProcessor.deserialize(message.getSubString(1, (int) message.length()));
                }
            }
            result.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtils.closeStatement(selectStmt);
        }
        return "";
    }

    /**
     * wrap query to limit result rows. The default implementation simply adds " LIMIT <i>&lt;limit&gt;</i>" to the
     * query. Subclasses may override this to modify the query statement different, eg for Oracle: "SELECT * from T
     * WHERE ROWNUM &lt;= 10".
     *
     * @return new query string
     */
    public String getResultLimitingQuery(String query, long limit) {
        return query + " LIMIT " + limit;
    }

    @Override
    public List<WorkflowSummary> selectWorkflowStateSummary(String poolid, String classname, Connection con) {
        PreparedStatement selectStmt = null;
        try {
            selectStmt = con.prepareStatement(
                    "select CLASSNAME, STATE, count(*) from COP_WORKFLOW_INSTANCE \n" +
                            "WHERE\n" +
                            "	(? is null or PPOOL_ID=?) AND \n" +
                            "	(? is null or CLASSNAME like ?) GROUP BY CLASSNAME,STATE");
            int pIdx = 1;
            pIdx = setFilterParam(selectStmt, poolid, java.sql.Types.VARCHAR, pIdx);
            pIdx = setFilterParam(selectStmt, classname == null ? null : "%" + classname + "%", java.sql.Types.VARCHAR, pIdx);

            selectStmt.setFetchSize(100);
            ResultSet resultSet = selectStmt.executeQuery();

            Map<String, WorkflowSummary> classNameToSummary = new HashMap<String, WorkflowSummary>();
            while (resultSet.next()) {
                String instanceClassname = resultSet.getString(1);
                WorkflowSummary summary = classNameToSummary.get(instanceClassname);
                if (summary == null) {
                    summary = new WorkflowSummary();
                    summary.setClassDescription(new WorkflowClassMetaData(instanceClassname, "", 1L, 1L, 1L, ""));
                    summary.setStateSummary(new WorkflowStateSummary(new HashMap<WorkflowInstanceState, Integer>()));
                    classNameToSummary.put(instanceClassname, summary);
                    for (WorkflowInstanceState s : WorkflowInstanceState.values())
                        summary.getStateSummary().getNumberOfWorkflowInstancesWithState().put(s, 0);
                }
                int status = resultSet.getInt(2);
                int count = resultSet.getInt(3);

                summary.getStateSummary().getNumberOfWorkflowInstancesWithState().put(DBProcessingStateWorkaround.fromKey(status).asWorkflowInstanceState(), count);
            }
            resultSet.close();
            ArrayList<WorkflowSummary> wfSummaryList = new ArrayList<WorkflowSummary>(classNameToSummary.values());
            for(WorkflowSummary wfSummary : wfSummaryList) {
                wfSummary.setComputedTotalCount();
            }
            return wfSummaryList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtils.closeStatement(selectStmt);
        }
    }

    private int setFilterParam(PreparedStatement stmt, Object value, int sqltype, int nextindex) throws SQLException {
        boolean isEmptyString = (value instanceof String && ((String) value).isEmpty());
        if (value != null && !isEmptyString) {
            if (value instanceof Date && sqltype == Types.DATE) {
                stmt.setObject(nextindex++, new java.sql.Timestamp(((Date) value).getTime()), Types.TIMESTAMP);
                stmt.setObject(nextindex++, new java.sql.Timestamp(((Date) value).getTime()), Types.TIMESTAMP);
            } else {
                stmt.setObject(nextindex++, value, sqltype);
                stmt.setObject(nextindex++, value, sqltype);
            }
        } else {
            stmt.setNull(nextindex++, sqltype);
            stmt.setNull(nextindex++, sqltype);
        }
        return nextindex;
    }

    protected String createWorkflowInstanceListQuery() {
        String subselectEXCEPTION = getResultLimitingQuery("SELECT \"EXCEPTION\"  FROM COP_WORKFLOW_INSTANCE_ERROR WHERE WORKFLOW_INSTANCE_ID = MASTER.ID ORDER BY ERROR_TS DESC", 1);
        String subselectERROR_TS = getResultLimitingQuery("SELECT ERROR_TS FROM COP_WORKFLOW_INSTANCE_ERROR WHERE WORKFLOW_INSTANCE_ID = MASTER.ID ORDER BY ERROR_TS DESC", 1);
        String stmt =
                "SELECT ID,STATE,PRIORITY,LAST_MOD_TS,PPOOL_ID,TIMEOUT,CREATION_TS, (" + subselectEXCEPTION + "), (" + subselectERROR_TS + "), LAST_MOD_TS as FINISHED_TS, CLASSNAME \n" +
                        "FROM COP_WORKFLOW_INSTANCE as master \n" +
                        "WHERE\n" +
                        "	(? is null or PPOOL_ID=?) AND \n" +
                        "	(? is null or CLASSNAME like ?) AND \n" +
                        "	(? is null or STATE=?) AND \n" +
                        "	(? is null or CREATION_TS>=?) AND \n" +
                        "	(? is null or CREATION_TS<=?) AND \n" +
                        "	(? is null or ID<=?) AND \n" +
                        "	(? is null or PRIORITY=?)";
        return stmt;
    }

    @Override
    public List<WorkflowInstanceInfo> selectWorkflowInstanceList(String poolid, String classname,
            WorkflowInstanceState state, Integer priority, Date from, Date to, String instanceId, long resultRowLimit, Connection con) {
        PreparedStatement selectStmt = null;
        try {
            String stmt = getResultLimitingQuery(createWorkflowInstanceListQuery(), resultRowLimit);
            selectStmt = con.prepareStatement(stmt);

            int pIdx = 1;
            pIdx = setFilterParam(selectStmt, poolid, java.sql.Types.VARCHAR, pIdx);
            pIdx = setFilterParam(selectStmt, classname == null ? null : "%" + classname + "%", java.sql.Types.VARCHAR, pIdx);
            pIdx = setFilterParam(selectStmt, (state == null ? null : DBProcessingStateWorkaround.fromWorkflowInstanceState(state).key()), java.sql.Types.INTEGER, pIdx);
            // pIdx = setFilterParam(selectStmt,from!=null?new
            // java.sql.Date(from.getTime()):null,java.sql.Types.DATE,pIdx);
            // pIdx = setFilterParam(selectStmt,to!=null?new java.sql.Date(to.getTime()):null,java.sql.Types.DATE,pIdx);
            pIdx = setFilterParam(selectStmt, from, java.sql.Types.DATE, pIdx);
            pIdx = setFilterParam(selectStmt, to, java.sql.Types.DATE, pIdx);
            pIdx = setFilterParam(selectStmt, instanceId, java.sql.Types.VARCHAR, pIdx);
            pIdx = setFilterParam(selectStmt, priority, java.sql.Types.INTEGER, pIdx);

            selectStmt.setFetchSize(100);

            ResultSet resultSet = selectStmt.executeQuery();

            ArrayList<WorkflowInstanceInfo> instances = new ArrayList<WorkflowInstanceInfo>();
            java.sql.ResultSetMetaData rsmd = resultSet.getMetaData();
            boolean exceptionIsClob = rsmd.getColumnType(8) == Types.CLOB;
            while (resultSet.next()) {
                WorkflowInstanceInfo workflowInstanceInfo = new WorkflowInstanceInfo();
                workflowInstanceInfo.setId(resultSet.getString(1));
                workflowInstanceInfo.setState(DBProcessingStateWorkaround.fromKey(resultSet.getInt(2)).asWorkflowInstanceState());
                workflowInstanceInfo.setPriority(resultSet.getInt(3));
                workflowInstanceInfo.setLastActivityTimestamp(new Date(resultSet.getTimestamp(4).getTime()));
                workflowInstanceInfo.setProcessorPoolId(resultSet.getString(5));
                workflowInstanceInfo.setTimeout(resultSet.getTimestamp(6) != null ? new Date(resultSet.getTimestamp(6).getTime()) : null);
                workflowInstanceInfo.setStartTime(new Date(resultSet.getTimestamp(7).getTime()));
                if (exceptionIsClob) {
                    Clob errorinfo = resultSet.getClob(8);
                    if (errorinfo != null) {
                        workflowInstanceInfo.setErrorInfos(errorinfo.getSubString(1, (int) errorinfo.length()));
                    }
                } else {
                    workflowInstanceInfo.setErrorInfos(resultSet.getString(8));
                }
                workflowInstanceInfo.setLastErrorTime(resultSet.getTimestamp(9) != null ? new Date(resultSet.getTimestamp(9).getTime()) : null);
                Date lastMod = resultSet.getTimestamp(10) != null ? new Date(resultSet.getTimestamp(10).getTime()) : null;
                if (workflowInstanceInfo.getState() == WorkflowInstanceState.FINISHED) {
                    workflowInstanceInfo.setFinishTime(lastMod);
                }
                workflowInstanceInfo.setOverallLifetimeInMs(System.currentTimeMillis() - workflowInstanceInfo.getStartTime().getTime());
                workflowInstanceInfo.setClassname(resultSet.getString(11));

                instances.add(workflowInstanceInfo);
            }
            resultSet.close();
            return instances;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtils.closeStatement(selectStmt);
        }
    }

    @Override
    public List<String[]> executeMonitoringQuery(String query, long resultRowLimit, Connection con) {
        PreparedStatement selectStmt = null;
        try {
            selectStmt = con.prepareStatement(getResultLimitingQuery(query, resultRowLimit));
            selectStmt.setFetchSize(100);

            ResultSet resultSet = selectStmt.executeQuery();

            ArrayList<String[]> result = new ArrayList<String[]>();
            java.sql.ResultSetMetaData rsmd = resultSet.getMetaData();
            int columnCount = rsmd.getColumnCount();
            String[] header = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                header[i - 1] = rsmd.getColumnLabel(i);
            }
            result.add(header);
            while (resultSet.next()) {
                String[] row = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = resultSet.getString(i);
                }
                result.add(row);
            }
            resultSet.close();
            return result;
        } catch (SQLException e) {
            logger.warn("", e);
            ArrayList<String[]> result = new ArrayList<String[]>();
            result.add(new String[] { "Error" });
            result.add(new String[] { e.getMessage() });
            return result;
        } finally {
            JdbcUtils.closeStatement(selectStmt);
        }
    }

    @Override
    public List<MessageInfo> selectMessages(boolean ignoreProcessed, long resultRowLimit, Connection con) {
        PreparedStatement selectStmt = null;
        try {
            selectStmt = con.prepareStatement(getResultLimitingQuery(
                    getSelectMessagesQuery(ignoreProcessed), resultRowLimit));

            selectStmt.setFetchSize(100);

            ResultSet resultSet = selectStmt.executeQuery();

            ArrayList<MessageInfo> logs = new ArrayList<MessageInfo>();
            while (resultSet.next()) {
                MessageInfo messageInfo = new MessageInfo();
                messageInfo.setCorrelationId(resultSet.getString(1));

                String response = resultSet.getString(2);
                if (response == null)
                    response = resultSet.getString(3);
                if (response != null) {
                    try {
                        Response<?> responseObject = (Response<?>) serializer.deserializeResponse(response);
                        messageInfo.setMessage(responseObject.toString());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                messageInfo.setTimestamp(resultSet.getTimestamp(4) != null ? new Date(resultSet.getTimestamp(4).getTime()) : null);
                messageInfo.setTimeout(resultSet.getTimestamp(5) != null ? new Date(resultSet.getTimestamp(5).getTime()) : null);
                logs.add(messageInfo);
            }
            resultSet.close();
            return logs;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            JdbcUtils.closeStatement(selectStmt);
        }
    }

    protected String getSelectMessagesQuery(boolean ignoreProcessed) {
        return "SELECT CORRELATION_ID, r.response, r.response, RESPONSE_TS, RESPONSE_TIMEOUT FROM COP_RESPONSE r "
                + (!ignoreProcessed ? "" : "WHERE not exists(select * from COP_WAIT w where r.CORRELATION_ID=w.CORRELATION_ID)");
    }

    @Override
    public String selectDatabaseMonitoringHtmlReport(Connection con) {
        return "<html><body>not supported for <b>" + this.getClass().getName() + "</b></body></html>";
    }

    @Override
    public String selectDatabaseMonitoringHtmlDetailReport(String sqlid, Connection con) {
        return "<html><body>not supported for <b>" + this.getClass().getName() + "</b></body></html>";
    }

    @Override
    public String getRecommendationsReport(String sqlid, Connection con) {
        return "not supported for " + this.getClass().getName();
    }

}
