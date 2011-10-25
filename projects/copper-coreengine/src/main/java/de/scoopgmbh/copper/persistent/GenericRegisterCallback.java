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
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.WaitMode;
import de.scoopgmbh.copper.batcher.AbstractBatchCommand;
import de.scoopgmbh.copper.batcher.BatchExecutor;
import de.scoopgmbh.copper.batcher.NullCallback;
import de.scoopgmbh.copper.db.utility.RetryingTransaction;

class GenericRegisterCallback {

	private static final Logger logger = Logger.getLogger(GenericRegisterCallback.class);

	static final class Command extends AbstractBatchCommand<Executor, Command> {

		private final DataSource dataSource;
		private final RegisterCall registerCall;
		private final Serializer serializer;

		@SuppressWarnings("unchecked")
		public Command(RegisterCall registerCall, DataSource dataSource, Serializer serializer) {
			super(NullCallback.instance,250);
			this.dataSource = dataSource;
			this.registerCall = registerCall;
			this.serializer = serializer;
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
						final Timestamp now = new Timestamp(System.currentTimeMillis());
						boolean doDeletes = false;
						PreparedStatement deleteWait = getConnection().prepareStatement("DELETE FROM COP_WAIT WHERE CORRELATION_ID=?");
						PreparedStatement deleteResponse = getConnection().prepareStatement("DELETE FROM COP_RESPONSE WHERE CORRELATION_ID=?");
						PreparedStatement insertWaitStmt = getConnection().prepareStatement("INSERT INTO COP_WAIT (CORRELATION_ID,WORKFLOW_INSTANCE_ID,MIN_NUMB_OF_RESP,TIMEOUT_TS,STATE,PRIORITY,PPOOL_ID,WFI_ROWID) VALUES (?,?,?,?,?,?,?,?)");
						PreparedStatement updateWfiStmt = getConnection().prepareStatement("UPDATE COP_WORKFLOW_INSTANCE SET STATE=?, PRIORITY=?, LAST_MOD_TS=?, PPOOL_ID=?, DATA=?, LONG_DATA=?, CS_WAITMODE=?, MIN_NUMB_OF_RESP=?, NUMB_OF_WAITS=?, TIMEOUT=? WHERE ID=?");
						for (Command cmd : commands) {
							RegisterCall rc = cmd.registerCall;
							for (String cid : rc.correlationIds) {
								insertWaitStmt.setString(1, cid);
								insertWaitStmt.setString(2, rc.workflow.getId());
								insertWaitStmt.setInt(3,rc.waitMode == WaitMode.ALL ? rc.correlationIds.length : 1);
								insertWaitStmt.setTimestamp(4,rc.timeoutTS);
								insertWaitStmt.setInt(5,0);
								insertWaitStmt.setInt(6,rc.workflow.getPriority());
								insertWaitStmt.setString(7,rc.workflow.getProcessorPoolId());
								insertWaitStmt.setString(8,((PersistentWorkflow<?>)rc.workflow).rowid);
								insertWaitStmt.addBatch();
							}
							int idx=1;
							String data = cmd.serializer.serializeWorkflow(rc.workflow);
							updateWfiStmt.setInt(idx++, DBProcessingState.WAITING.ordinal());
							updateWfiStmt.setInt(idx++, rc.workflow.getPriority());
							updateWfiStmt.setTimestamp(idx++, now);
							updateWfiStmt.setString(idx++, rc.workflow.getProcessorPoolId());
							updateWfiStmt.setString(idx++, data.length() > 4000 ? null : data);
							updateWfiStmt.setString(idx++, data.length() > 4000 ? data : null);
							updateWfiStmt.setInt(idx++, rc.waitMode.ordinal());
							updateWfiStmt.setInt(idx++, rc.waitMode == WaitMode.FIRST ? 1 : rc.correlationIds.length);
							updateWfiStmt.setInt(idx++, rc.correlationIds.length);
							updateWfiStmt.setTimestamp(idx++, rc.timeoutTS);
							updateWfiStmt.setString(idx++, rc.workflow.getId());
							updateWfiStmt.addBatch();

							List<String> cidList = ((PersistentWorkflow<?>)rc.workflow).cidList;
							if (cidList != null) {
								for (String cid : cidList) {
									deleteResponse.setString(1, cid);
									deleteResponse.addBatch();

									deleteWait.setString(1, cid);
									deleteWait.addBatch();

									doDeletes = true;
								}
							}
						}
						insertWaitStmt.executeBatch();
						updateWfiStmt.executeBatch();
						if (doDeletes) deleteResponse.executeBatch();
						if (doDeletes) deleteWait.executeBatch();
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
