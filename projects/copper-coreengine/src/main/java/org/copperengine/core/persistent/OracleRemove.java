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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.copperengine.core.Acknowledge;
import org.copperengine.core.batcher.AbstractBatchCommand;
import org.copperengine.core.batcher.AcknowledgeCallbackWrapper;
import org.copperengine.core.batcher.BatchCommand;
import org.copperengine.core.batcher.BatchExecutor;
import org.copperengine.core.db.utility.JdbcUtils;

class OracleRemove {

    static final class Command extends AbstractBatchCommand<Executor, Command> {

        private final PersistentWorkflow<?> wf;
        private final boolean remove;
        private final WorkflowPersistencePlugin workflowPersistencePlugin;

        public Command(PersistentWorkflow<?> wf, boolean remove, final long targetTime, WorkflowPersistencePlugin workflowPersistencePlugin, Acknowledge ack) {
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
        public int maximumBatchSize() {
            return 100;
        }

        @Override
        public int preferredBatchSize() {
            return 50;
        }

        public void doExec(final Collection<BatchCommand<Executor, Command>> commands, final Connection c) throws Exception {
            final PreparedStatement stmtDelQueue = c.prepareStatement("DELETE FROM COP_QUEUE WHERE WFI_ROWID=? AND PPOOL_ID=? AND PRIORITY=?");
            final PreparedStatement stmtDelResponse = c.prepareStatement("DELETE FROM COP_RESPONSE WHERE CORRELATION_ID=?");
            final PreparedStatement stmtDelWait = c.prepareStatement("DELETE FROM COP_WAIT WHERE CORRELATION_ID=?");
            final PreparedStatement stmtDelErrors = c.prepareStatement("DELETE FROM COP_WORKFLOW_INSTANCE_ERROR WHERE WORKFLOW_INSTANCE_ID=?");
            final PreparedStatement stmtDelBP = ((Command) commands.iterator().next()).remove ? c.prepareStatement("DELETE FROM COP_WORKFLOW_INSTANCE WHERE ID=?") : c.prepareStatement("UPDATE COP_WORKFLOW_INSTANCE SET STATE=" + DBProcessingState.FINISHED.ordinal() + ", LAST_MOD_TS=SYSTIMESTAMP WHERE ID=?");
            try {
                HashMap<WorkflowPersistencePlugin, ArrayList<PersistentWorkflow<?>>> wfs = new HashMap<WorkflowPersistencePlugin, ArrayList<PersistentWorkflow<?>>>();
                boolean cidsFound = false;
                for (BatchCommand<Executor, Command> _cmd : commands) {
                    Command cmd = (Command) _cmd;
                    PersistentWorkflow<?> persistentWorkflow = (PersistentWorkflow<?>) cmd.wf;
                    persistentWorkflow.flushCheckpointAcknowledges();

                    if (cmd.wf.waitCidList != null) {
                        for (String cid : cmd.wf.waitCidList) {
                            stmtDelResponse.setString(1, cid);
                            stmtDelResponse.addBatch();
                            stmtDelWait.setString(1, cid);
                            stmtDelWait.addBatch();
                            if (!cidsFound)
                                cidsFound = true;
                        }
                    }
                    stmtDelBP.setString(1, cmd.wf.getId());
                    stmtDelBP.addBatch();

                    stmtDelErrors.setString(1, cmd.wf.getId());
                    stmtDelErrors.addBatch();

                    stmtDelQueue.setString(1, cmd.wf.rowid);
                    stmtDelQueue.setString(2, cmd.wf.oldProcessorPoolId);
                    stmtDelQueue.setInt(3, cmd.wf.oldPrio);
                    stmtDelQueue.addBatch();

                    ArrayList<PersistentWorkflow<?>> _wfs = wfs.get(cmd.workflowPersistencePlugin);
                    if (_wfs == null) {
                        _wfs = new ArrayList<PersistentWorkflow<?>>();
                        wfs.put(cmd.workflowPersistencePlugin, _wfs);
                    }
                    _wfs.add(cmd.wf);

                }
                if (cidsFound) {
                    stmtDelResponse.executeBatch();
                    stmtDelWait.executeBatch();
                }
                stmtDelBP.executeBatch();
                stmtDelErrors.executeBatch();
                stmtDelQueue.executeBatch();

                for (Map.Entry<WorkflowPersistencePlugin, ArrayList<PersistentWorkflow<?>>> en : wfs.entrySet()) {
                    en.getKey().onWorkflowsSaved(c, en.getValue());
                }
            } finally {
                JdbcUtils.closeStatement(stmtDelQueue);
                JdbcUtils.closeStatement(stmtDelResponse);
                JdbcUtils.closeStatement(stmtDelWait);
                JdbcUtils.closeStatement(stmtDelErrors);
                JdbcUtils.closeStatement(stmtDelBP);
            }
        }

    }
}
