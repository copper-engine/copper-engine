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
package de.scoopgmbh.copper.monitoring.server.persistent;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.copperengine.core.persistent.txn.DatabaseTransaction;
import org.copperengine.core.persistent.txn.TransactionController;

import de.scoopgmbh.copper.monitoring.core.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitoring.core.model.MessageInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowStateSummary;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowSummary;

public class MonitoringDbStorage {

    private final DatabaseMonitoringDialect dialect;
    private final TransactionController transactionController;

    public MonitoringDbStorage(TransactionController transactionController, DatabaseMonitoringDialect dialect) {
        this.transactionController = transactionController;
        this.dialect = dialect;
    }

    protected <T> T run(final DatabaseTransaction<T> txn) throws Exception {
        return transactionController.run(txn);
    }

    public WorkflowStateSummary selectTotalWorkflowStateSummary() {
        try {
            return run(new DatabaseTransaction<WorkflowStateSummary>() {
                @Override
                public WorkflowStateSummary run(Connection con) throws Exception {
                    return dialect.selectTotalWorkflowStateSummary(con);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<AuditTrailInfo> selectAuditTrails(final String workflowClass, final String workflowInstanceId, final String correlationId, final Integer level, final long resultRowLimit) {
        try {
            return run(new DatabaseTransaction<List<AuditTrailInfo>>() {
                @Override
                public List<AuditTrailInfo> run(Connection con) throws Exception {
                    return dialect.selectAuditTrails(workflowClass, workflowInstanceId, correlationId, level, resultRowLimit, con);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String selectAuditTrailMessage(final long id) {
        try {
            return run(new DatabaseTransaction<String>() {
                @Override
                public String run(Connection con) throws Exception {
                    return dialect.selectAuditTrailMessage(id, con);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<WorkflowSummary> selectWorkflowSummary(final String poolid, final String classname) {
        try {
            return run(new DatabaseTransaction<List<WorkflowSummary>>() {
                @Override
                public List<WorkflowSummary> run(Connection con) throws Exception {
                    return dialect.selectWorkflowStateSummary(poolid, classname, con);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<WorkflowInstanceInfo> selectWorkflowInstanceList(final String poolid, final String classname,
            final WorkflowInstanceState state, final Integer priority, final Date from, final Date to, final String instanceId, final long resultRowLimit) {
        try {
            return run(new DatabaseTransaction<List<WorkflowInstanceInfo>>() {
                @Override
                public List<WorkflowInstanceInfo> run(Connection con) throws Exception {
                    return dialect.selectWorkflowInstanceList(poolid, classname, state, priority, from, to, instanceId, resultRowLimit, con);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String[]> executeMonitoringQuery(final String query, final long resultRowLimit) {
        try {
            return run(new DatabaseTransaction<List<String[]>>() {
                @Override
                public List<String[]> run(Connection con) throws Exception {
                    return dialect.executeMonitoringQuery(query, resultRowLimit, con);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<MessageInfo> selectMessages(final boolean ignoreProcessed, final long resultRowLimit) {
        try {
            return run(new DatabaseTransaction<List<MessageInfo>>() {
                @Override
                public List<MessageInfo> run(Connection con) throws Exception {
                    return dialect.selectMessages(ignoreProcessed, resultRowLimit, con);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getDatabaseMonitoringHtmlReport() {
        try {
            return run(new DatabaseTransaction<String>() {
                @Override
                public String run(Connection con) throws Exception {
                    return dialect.selectDatabaseMonitoringHtmlReport(con);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getDatabaseMonitoringHtmlDetailReport(final String sqlid) {
        try {
            return run(new DatabaseTransaction<String>() {
                @Override
                public String run(Connection con) throws Exception {
                    return dialect.selectDatabaseMonitoringHtmlDetailReport(sqlid, con);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getRecommendationsReport(final String sqlid) {
        try {
            return run(new DatabaseTransaction<String>() {
                @Override
                public String run(Connection con) throws Exception {
                    return dialect.getRecommendationsReport(sqlid, con);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
