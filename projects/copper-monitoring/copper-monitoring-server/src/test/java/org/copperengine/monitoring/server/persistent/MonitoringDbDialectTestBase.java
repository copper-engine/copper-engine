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
package org.copperengine.monitoring.server.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.copperengine.core.InterruptException;
import org.copperengine.core.Response;
import org.copperengine.core.audit.BatchingAuditTrail;
import org.copperengine.core.batcher.RetryingTxnBatchRunner;
import org.copperengine.core.batcher.impl.BatcherImpl;
import org.copperengine.core.db.utility.RetryingTransaction;
import org.copperengine.core.instrument.Transformed;
import org.copperengine.core.persistent.DatabaseDialect;
import org.copperengine.core.persistent.PersistentWorkflow;
import org.copperengine.monitoring.core.model.AuditTrailInfo;
import org.copperengine.monitoring.core.model.MessageInfo;
import org.copperengine.monitoring.core.model.WorkflowInstanceInfo;
import org.copperengine.monitoring.core.model.WorkflowInstanceState;
import org.copperengine.monitoring.core.model.WorkflowStateSummary;
import org.copperengine.monitoring.core.model.WorkflowSummary;
import org.junit.Before;
import org.junit.Test;

public abstract class MonitoringDbDialectTestBase {

    protected DataSource datasource;
    protected DatabaseMonitoringDialect monitoringDbDialect;
    protected DatabaseDialect databaseDialect;

    abstract void intit();

    @Before
    public void setUp() {
        intit();
    }

    @Test
    public void test_selectTotalWorkflowSummary() throws SQLException, Exception {
        DummyPersistentWorkflow1 wf = new DummyPersistentWorkflow1("id", "ppoolId", "1", 1);
        databaseDialect.insert(wf, datasource.getConnection());

        try {
            WorkflowStateSummary selectedWorkflowStateSummary = monitoringDbDialect.selectTotalWorkflowStateSummary(datasource.getConnection());
            assertTrue(selectedWorkflowStateSummary.getNumberOfWorkflowInstancesWithState().size() > 0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void test_selectAudittrail() throws SQLException, Exception {

        BatcherImpl batcher = new BatcherImpl(3);
        @SuppressWarnings("rawtypes")
        RetryingTxnBatchRunner<?, ?> batchRunner = new RetryingTxnBatchRunner();
        batchRunner.setDataSource(datasource);
        batcher.setBatchRunner(batchRunner);
        batcher.startup();
        BatchingAuditTrail auditTrail = new BatchingAuditTrail();
        auditTrail.setBatcher(batcher);
        auditTrail.setDataSource(datasource);
        // auditTrail.setMessagePostProcessor(new CompressedBase64PostProcessor());
        try {
            auditTrail.startup();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Date occurrence = new Date();
        auditTrail.synchLog(1, occurrence, "1", "2", "", "", "", "detail", "Text");

        try {
            List<AuditTrailInfo> selectAuditTrails = monitoringDbDialect.selectAuditTrails(null, null, null, null, 3, datasource.getConnection());
            assertEquals(1, selectAuditTrails.size());
            assertEquals(occurrence.getTime(), selectAuditTrails.get(0).getOccurrence().getTime());
            assertEquals(1, selectAuditTrails.get(0).getLoglevel());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        batcher.shutdown();
    }

    @Transformed
    public static class DummyPersistentWorkflow1 extends PersistentWorkflow<Serializable> {

        private static final long serialVersionUID = 7047352707643389609L;

        public DummyPersistentWorkflow1(String id, String ppoolId, String rowid, int prio) {
            if (id == null)
                throw new NullPointerException();
            if (ppoolId == null)
                throw new NullPointerException();
            setId(id);
            setProcessorPoolId(ppoolId);
            setPriority(prio);
        }

        @Override
        public void main() throws InterruptException {
        }
    }

    @Transformed
    public static class DummyPersistentWorkflow2 extends PersistentWorkflow<Serializable> {

        private static final long serialVersionUID = 7047352707643389609L;

        public DummyPersistentWorkflow2(String id, String ppoolId, String rowid, int prio) {
            if (id == null)
                throw new NullPointerException();
            if (ppoolId == null)
                throw new NullPointerException();
            setId(id);
            setProcessorPoolId(ppoolId);
            setPriority(prio);
        }

        @Override
        public void main() throws InterruptException {
        }
    }

    @Test
    public void test_selectWorkflowSummary() throws SQLException, Exception {
        {
            DummyPersistentWorkflow1 wf = new DummyPersistentWorkflow1("id1", "P#DEFAULT", "1", 1);
            databaseDialect.insert(wf, datasource.getConnection());
        }
        {
            DummyPersistentWorkflow1 wf = new DummyPersistentWorkflow1("id2", "P#DEFAULT", "2", 1);
            databaseDialect.insert(wf, datasource.getConnection());
        }
        {
            DummyPersistentWorkflow2 wf = new DummyPersistentWorkflow2("id3", "P#DEFAULT", "3", 1);
            databaseDialect.insert(wf, datasource.getConnection());
        }

        try {
            List<WorkflowSummary> selectSummary = monitoringDbDialect.selectWorkflowStateSummary(null, null, datasource.getConnection());
            assertEquals(2, selectSummary.size());

            assertEquals("org.copperengine.monitoring.server.persistent.MonitoringDbDialectTestBase$DummyPersistentWorkflow1", selectSummary.get(0).getClassDescription().getClassname());
            assertEquals(2, selectSummary.get(0).getStateSummary().getCount(WorkflowInstanceState.ENQUEUED));

            assertEquals("org.copperengine.monitoring.server.persistent.MonitoringDbDialectTestBase$DummyPersistentWorkflow2", selectSummary.get(1).getClassDescription().getClassname());
            assertEquals(1, selectSummary.get(1).getStateSummary().getCount(WorkflowInstanceState.ENQUEUED));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void test_selectWorkflowinstance() throws SQLException, Exception {
        {
            DummyPersistentWorkflow1 wf = new DummyPersistentWorkflow1("id1", "P#DEFAULT", "1", 1);
            databaseDialect.insert(wf, datasource.getConnection());
        }
        {
            DummyPersistentWorkflow1 wf = new DummyPersistentWorkflow1("id2", "P#DEFAULT", "2", 1);
            databaseDialect.insert(wf, datasource.getConnection());
        }
        {
            DummyPersistentWorkflow2 wf = new DummyPersistentWorkflow2("id3", "P#DEFAULT", "3", 1);
            databaseDialect.insert(wf, datasource.getConnection());
        }

        try {
            List<WorkflowInstanceInfo> selectInstances = monitoringDbDialect.selectWorkflowInstanceList(null, null, null, null, null, null, null, 1000, datasource.getConnection());
            assertEquals(3, selectInstances.size());
            // assertEquals(2,selectSummary.get(0).getStateSummary().getCount(WorkflowInstanceState.ENQUEUED));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test_selectWorkflowinstance_timeframe() throws SQLException, Exception {
        {
            DummyPersistentWorkflow1 wf = new DummyPersistentWorkflow1("id1", "P#DEFAULT", "1", 1);
            databaseDialect.insert(wf, datasource.getConnection());
        }
        {
            DummyPersistentWorkflow1 wf = new DummyPersistentWorkflow1("id2", "P#DEFAULT", "2", 1);
            databaseDialect.insert(wf, datasource.getConnection());
        }
        {
            DummyPersistentWorkflow2 wf = new DummyPersistentWorkflow2("id3", "P#DEFAULT", "3", 1);
            databaseDialect.insert(wf, datasource.getConnection());
        }

        try {
            {
                List<WorkflowInstanceInfo> selectInstances = monitoringDbDialect.selectWorkflowInstanceList(null, null, null, null, new Date(1), null, null, 1000, datasource.getConnection());
                assertEquals(3, selectInstances.size());
            }
            {
                List<WorkflowInstanceInfo> selectInstances = monitoringDbDialect.selectWorkflowInstanceList(null, null, null, null, null, new Date(System.currentTimeMillis() + 10000), null, 1000, datasource.getConnection());
                assertEquals(3, selectInstances.size());
            }
            // assertEquals(2,selectSummary.get(0).getStateSummary().getCount(WorkflowInstanceState.ENQUEUED));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test_selectMessages() throws SQLException, Exception {
        final Response<String> response = new Response<String>("test123");
        response.setResponseId("5456465");
        List<Response<?>> list = new ArrayList<Response<?>>();
        list.add(response);
        databaseDialect.notify(list, datasource.getConnection());

        try {
            List<MessageInfo> messages = monitoringDbDialect.selectMessages(false, 1000, datasource.getConnection());
            assertEquals(1, messages.size());

            List<MessageInfo> messages2 = monitoringDbDialect.selectMessages(true, 1000, datasource.getConnection());
            assertEquals(1, messages2.size());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void cleanDB(DataSource ds) {
        try {
            new RetryingTransaction<Void>(ds) {
                @Override
                protected Void execute() throws Exception {
                    Statement stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_AUDIT_TRAIL_EVENT");
                    stmt.close();
                    stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_WAIT");
                    stmt.close();
                    stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_RESPONSE");
                    stmt.close();
                    stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_QUEUE");
                    stmt.close();
                    stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_WORKFLOW_INSTANCE");
                    stmt.close();
                    stmt = createStatement(getConnection());
                    stmt.execute("DELETE FROM COP_WORKFLOW_INSTANCE_ERROR");
                    stmt.close();
                    return null;
                }
            }.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Statement createStatement(Connection con) throws SQLException {
        return con.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY,
                ResultSet.CLOSE_CURSORS_AT_COMMIT);
    }

}
