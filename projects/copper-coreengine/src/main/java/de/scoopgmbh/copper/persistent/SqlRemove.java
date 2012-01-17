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
import java.sql.Timestamp;
import java.util.Collection;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.batcher.AbstractBatchCommand;
import de.scoopgmbh.copper.batcher.BatchCommand;
import de.scoopgmbh.copper.batcher.BatchExecutor;
import de.scoopgmbh.copper.batcher.NullCallback;

class SqlRemove {

	private static final Logger logger = Logger.getLogger(SqlRemove.class);

	static final class Command extends AbstractBatchCommand<Executor, Command> {

		private final PersistentWorkflow<?> wf;
		private final boolean remove;

		@SuppressWarnings("unchecked")
		public Command(PersistentWorkflow<?> wf, DataSource dataSource, boolean remove) {
			super(NullCallback.instance,dataSource,250);
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
		protected void doExec(final Collection<BatchCommand<Executor, Command>> commands, final Connection con) throws Exception {
			final Timestamp NOW = new Timestamp(System.currentTimeMillis());
			final boolean remove = ((Command)commands.iterator().next()).remove;
			final PreparedStatement stmtDelQueue = con.prepareStatement("DELETE FROM COP_QUEUE WHERE WORKFLOW_INSTANCE_ID=? AND PPOOL_ID=? AND PRIORITY=?");
			final PreparedStatement stmtDelResponse = con.prepareStatement("DELETE FROM COP_RESPONSE WHERE CORRELATION_ID=?");
			final PreparedStatement stmtDelWait = con.prepareStatement("DELETE FROM COP_WAIT WHERE CORRELATION_ID=?");
			final PreparedStatement stmtDelBP = remove ? con.prepareStatement("DELETE FROM COP_WORKFLOW_INSTANCE WHERE ID=?") : con.prepareStatement("UPDATE COP_WORKFLOW_INSTANCE SET STATE="+DBProcessingState.FINISHED.ordinal()+", LAST_MOD_TS=? WHERE ID=?");
			final PreparedStatement stmtDelErrors = con.prepareStatement("DELETE FROM COP_WORKFLOW_INSTANCE_ERROR WHERE WORKFLOW_INSTANCE_ID=?");
			boolean cidsFound = false;
			for (BatchCommand<Executor, Command> _cmd : commands) {
				Command cmd = (Command)_cmd;
				if (cmd.wf.cidList != null) {
					for (String cid : cmd.wf.cidList) {
						stmtDelResponse.setString(1, cid);
						stmtDelResponse.addBatch();
						stmtDelWait.setString(1, cid);
						stmtDelWait.addBatch();
						if (!cidsFound) cidsFound = true;
					}
				}
				int idx=1;
				if (!remove) {
					stmtDelBP.setTimestamp(idx++, NOW);
				}
				stmtDelBP.setString(idx++, cmd.wf.getId());
				stmtDelBP.addBatch();

				stmtDelErrors.setString(1, cmd.wf.getId());
				stmtDelErrors.addBatch();

				stmtDelQueue.setString(1, cmd.wf.getId());
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

		@Override
		public int maximumBatchSize() {
			return 100;
		}

		@Override
		public int preferredBatchSize() {
			return 50;
		}

	}
}
