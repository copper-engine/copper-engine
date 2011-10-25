/*
 * Copyright 2002-2011 SCOOP Software GmbH
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

import java.sql.PreparedStatement;
import java.util.Collection;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.batcher.AbstractBatchCommand;
import de.scoopgmbh.copper.batcher.BatchExecutor;
import de.scoopgmbh.copper.batcher.NullCallback;
import de.scoopgmbh.copper.db.utility.RetryingTransaction;

class GenericRemove {

	private static final Logger logger = Logger.getLogger(GenericRemove.class);

	static final class Command extends AbstractBatchCommand<Executor, Command> {

		private final DataSource dataSource;
		private final PersistentWorkflow<?> wf;

		@SuppressWarnings("unchecked")
		public Command(PersistentWorkflow<?> wf, DataSource dataSource) {
			super(NullCallback.instance,250);
			this.dataSource = dataSource;
			this.wf = wf;
		}

		@Override
		public Executor executor() {
			return Executor.INSTANCE;
		}

	}

	static final class Executor extends BatchExecutor<Executor, Command> {

		private static final Executor INSTANCE = new Executor();

		@Override
		protected void executeCommands(final Collection<Command> commands) {
			if (commands.isEmpty())
				return;

			try {
				new RetryingTransaction(commands.iterator().next().dataSource) {
					@Override
					protected void execute() throws Exception {
						final PreparedStatement stmtDelResponse = getConnection().prepareStatement("DELETE FROM COP_RESPONSE WHERE CORRELATION_ID=?");
						final PreparedStatement stmtDelWait = getConnection().prepareStatement("DELETE FROM COP_WAIT WHERE CORRELATION_ID=?");
						final PreparedStatement stmtDelBP = getConnection().prepareStatement("DELETE FROM COP_WORKFLOW_INSTANCE WHERE ID=?");
						final PreparedStatement stmtDelErrors = getConnection().prepareStatement("DELETE FROM COP_WORKFLOW_INSTANCE_ERROR WHERE WORKFLOW_INSTANCE_ID=?");
						boolean cidsFound = false;
						for (Command cmd : commands) {
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
						}
						if (cidsFound) {
							stmtDelResponse.executeBatch();
							stmtDelWait.executeBatch();
						}
						stmtDelBP.executeBatch();
						stmtDelErrors.executeBatch();
					}
				}.run();
			} 
			catch (Exception e) {
				logger.error("",e);
				throw new RuntimeException(e);
				// todo Einzelverarbeitung...
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
}
