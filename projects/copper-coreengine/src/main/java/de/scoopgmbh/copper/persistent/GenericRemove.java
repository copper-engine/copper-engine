/*
 * Copyright 2002-2012 SCOOP Software GmbH
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
package de.scoopgmbh.copper.persistent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;

import org.springframework.jdbc.support.JdbcUtils;

import de.scoopgmbh.copper.batcher.AbstractBatchCommand;
import de.scoopgmbh.copper.batcher.BatchCommand;
import de.scoopgmbh.copper.batcher.BatchExecutor;
import de.scoopgmbh.copper.batcher.NullCallback;

class GenericRemove {

	static final class Command extends AbstractBatchCommand<Executor, Command> {

		private final PersistentWorkflow<?> wf;
		private final boolean remove;

		@SuppressWarnings("unchecked")
		public Command(PersistentWorkflow<?> wf, boolean remove) {
			super(NullCallback.instance,250);
			this.wf = wf;
			this.remove = remove;
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
			final PreparedStatement stmtDelBP = ((Command)commands.iterator().next()).remove ? c.prepareStatement("DELETE FROM COP_WORKFLOW_INSTANCE WHERE ID=?") : c.prepareStatement("UPDATE COP_WORKFLOW_INSTANCE SET STATE="+DBProcessingState.FINISHED.ordinal()+", LAST_MOD_TS=SYSTIMESTAMP WHERE ID=?");
			try {
				boolean cidsFound = false;
				for (BatchCommand<Executor, Command> _cmd : commands) {
					Command cmd = (Command) _cmd;
					if (cmd.wf.cidList != null) {
						for (String cid : cmd.wf.cidList) {
							stmtDelResponse.setString(1, cid);
							stmtDelResponse.addBatch();
							stmtDelWait.setString(1, cid);
							stmtDelWait.addBatch();
							if (!cidsFound) cidsFound = true;
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
				}
				if (cidsFound) {
					stmtDelResponse.executeBatch();
					stmtDelWait.executeBatch();
				}
				stmtDelBP.executeBatch();
				stmtDelErrors.executeBatch();
				stmtDelQueue.executeBatch();
			}
			finally {
				JdbcUtils.closeStatement(stmtDelQueue);
				JdbcUtils.closeStatement(stmtDelResponse);
				JdbcUtils.closeStatement(stmtDelWait);
				JdbcUtils.closeStatement(stmtDelErrors);
				JdbcUtils.closeStatement(stmtDelBP);
			}
		}

	}
}
