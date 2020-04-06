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
package org.copperengine.core.persistent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.batcher.AbstractBatchCommand;
import org.copperengine.core.batcher.AcknowledgeCallbackWrapper;
import org.copperengine.core.batcher.BatchCommand;
import org.copperengine.core.batcher.BatchExecutor;
import org.copperengine.core.db.utility.JdbcUtils;

class SqlRemove {

    static final class Command extends AbstractBatchCommand<Executor, Command> {

        private final PersistentWorkflow<?> wf;
        private final boolean remove;
        private final WorkflowPersistencePlugin workflowPersistencePlugin;

        public Command(PersistentWorkflow<?> wf, boolean remove, final long targetTime, final WorkflowPersistencePlugin workflowPersistencePlugin, Acknowledge ack) {
            super(new AcknowledgeCallbackWrapper<Command>(ack), targetTime);
            this.wf = wf;
            this.remove = remove;
            this.workflowPersistencePlugin = workflowPersistencePlugin;
        }

        @Override
        public Executor executor() {
            return Executor.INSTANCE;
        }

    }

    static final class Executor extends BatchExecutor<Executor, Command> {

        private static final Executor INSTANCE = new Executor();

        @Override
        public void doExec(final Collection<BatchCommand<Executor, Command>> commands, final Connection con) throws Exception {
            final Timestamp NOW = new Timestamp(System.currentTimeMillis());
            final boolean remove = ((Command) commands.iterator().next()).remove;
            PreparedStatement stmtDelQueue = null;
            PreparedStatement stmtDelResponse = null;
            PreparedStatement stmtDelWait = null;
            PreparedStatement stmtDelBP = null;
            PreparedStatement stmtDelErrors = null;
            try {
                final List<String> responseIds2delete = new ArrayList<>();
                HashMap<WorkflowPersistencePlugin, ArrayList<PersistentWorkflow<?>>> wfs = new HashMap<WorkflowPersistencePlugin, ArrayList<PersistentWorkflow<?>>>();
                stmtDelQueue = con.prepareStatement("DELETE FROM COP_QUEUE WHERE WORKFLOW_INSTANCE_ID=?");
                stmtDelResponse = con.prepareStatement("DELETE FROM COP_RESPONSE WHERE RESPONSE_ID=?");
                stmtDelWait = con.prepareStatement("DELETE FROM COP_WAIT WHERE CORRELATION_ID=?");
                stmtDelBP = remove ? con.prepareStatement("DELETE FROM COP_WORKFLOW_INSTANCE WHERE ID=?") : con.prepareStatement("UPDATE COP_WORKFLOW_INSTANCE SET STATE=" + DBProcessingState.FINISHED.ordinal() + ", LAST_MOD_TS=? WHERE ID=?");
                stmtDelErrors = con.prepareStatement("DELETE FROM COP_WORKFLOW_INSTANCE_ERROR WHERE WORKFLOW_INSTANCE_ID=?");

                boolean cidsFound = false;
                for (BatchCommand<Executor, Command> _cmd : commands) {
                    Command cmd = (Command) _cmd;
                    PersistentWorkflow<?> persistentWorkflow = (PersistentWorkflow<?>) cmd.wf;
                    persistentWorkflow.flushCheckpointAcknowledges();
                    if (cmd.wf.waitCidList != null) {
                        for (String cid : cmd.wf.waitCidList) {
                            stmtDelWait.setString(1, cid);
                            stmtDelWait.addBatch();
                            if (!cidsFound)
                                cidsFound = true;
                        }
                    }
                    int idx = 1;
                    if (!remove) {
                        stmtDelBP.setTimestamp(idx++, NOW);
                    }
                    stmtDelBP.setString(idx++, cmd.wf.getId());
                    stmtDelBP.addBatch();

                    stmtDelErrors.setString(1, cmd.wf.getId());
                    stmtDelErrors.addBatch();

                    stmtDelQueue.setString(1, cmd.wf.getId());
                    stmtDelQueue.addBatch();

                    ArrayList<PersistentWorkflow<?>> _wfs = wfs.get(cmd.workflowPersistencePlugin);
                    if (_wfs == null) {
                        _wfs = new ArrayList<PersistentWorkflow<?>>();
                        wfs.put(cmd.workflowPersistencePlugin, _wfs);
                    }
                    _wfs.add(cmd.wf);

                    if (persistentWorkflow.responseIdList != null)
                        responseIds2delete.addAll(persistentWorkflow.responseIdList);
                }
                if (cidsFound) {
                    stmtDelWait.executeBatch();
                }
                stmtDelQueue.executeBatch();
                stmtDelErrors.executeBatch();
                stmtDelBP.executeBatch();

                deleteResponses(stmtDelResponse, responseIds2delete, preferredBatchSize());

                for (Map.Entry<WorkflowPersistencePlugin, ArrayList<PersistentWorkflow<?>>> en : wfs.entrySet()) {
                    en.getKey().onWorkflowsSaved(con, en.getValue());
                }

            } finally {
                JdbcUtils.closeStatement(stmtDelQueue);
                JdbcUtils.closeStatement(stmtDelResponse);
                JdbcUtils.closeStatement(stmtDelWait);
                JdbcUtils.closeStatement(stmtDelBP);
                JdbcUtils.closeStatement(stmtDelErrors);
            }
        }

        @Override
        public int maximumBatchSize() {
            return 100;
        }

        @Override
        public int preferredBatchSize() {
            return 50;
        }

    }

    private static void deleteResponses(final PreparedStatement stmtDelResponse, final List<String> responseIds2delete, final int batchSize) throws SQLException {
        int count = 0;
        for (String id : responseIds2delete) {
            stmtDelResponse.setString(1, id);
            stmtDelResponse.addBatch();
            count++;
            if (count == batchSize) {
                stmtDelResponse.executeBatch();
                stmtDelResponse.clearBatch();
                count = 0;
            }
        }
        if (count > 0) {
            stmtDelResponse.executeBatch();
            stmtDelResponse.clearBatch();
            count = 0;
        }
    }
}
