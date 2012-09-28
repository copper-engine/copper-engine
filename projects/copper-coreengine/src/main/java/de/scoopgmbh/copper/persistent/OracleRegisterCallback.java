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

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.JdbcUtils;

import de.scoopgmbh.copper.WaitHook;
import de.scoopgmbh.copper.WaitMode;
import de.scoopgmbh.copper.batcher.AbstractBatchCommand;
import de.scoopgmbh.copper.batcher.BatchCommand;
import de.scoopgmbh.copper.batcher.BatchExecutor;
import de.scoopgmbh.copper.batcher.CommandCallback;

class OracleRegisterCallback {

	private static final Logger logger = LoggerFactory.getLogger(OracleRegisterCallback.class);

	static final class Command extends AbstractBatchCommand<Executor, Command> {

		private final RegisterCall registerCall;
		private final Serializer serializer;

		public Command(final RegisterCall registerCall, final Serializer serializer, final ScottyDBStorageInterface dbStorageInterface, final long targetTime) {
			super(new CommandCallback<Command>() {
				@Override
				public void commandCompleted() {
				}
				@SuppressWarnings("unchecked")
				@Override
				public void unhandledException(Exception e) {
					logger.error("Execution of batch entry in a single txn failed.",e);
					dbStorageInterface.error((PersistentWorkflow<Serializable>)registerCall.workflow, e);
				}
			},targetTime);
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
		public void doExec(final Collection<BatchCommand<Executor, Command>> commands, final Connection con) throws Exception {
			final Timestamp now = new Timestamp(System.currentTimeMillis());
			boolean doWaitDeletes = false;
			boolean doResponseDeletes = false;
			PreparedStatement stmtDelQueue = con.prepareStatement("DELETE FROM COP_QUEUE WHERE WFI_ROWID=? AND PPOOL_ID=? AND PRIORITY=?");
			PreparedStatement deleteWait = con.prepareStatement("DELETE FROM COP_WAIT WHERE CORRELATION_ID=?");
			PreparedStatement deleteResponse = con.prepareStatement("DELETE FROM COP_RESPONSE WHERE CORRELATION_ID=?");
			PreparedStatement insertWaitStmt = con.prepareStatement("INSERT INTO COP_WAIT (CORRELATION_ID,WORKFLOW_INSTANCE_ID,MIN_NUMB_OF_RESP,TIMEOUT_TS,STATE,PRIORITY,PPOOL_ID,WFI_ROWID) VALUES (?,?,?,?,?,?,?,?)");
			PreparedStatement updateWfiStmt = con.prepareStatement("UPDATE COP_WORKFLOW_INSTANCE SET STATE=?, PRIORITY=?, LAST_MOD_TS=?, PPOOL_ID=?, DATA=?, LONG_DATA=?, OBJECT_STATE=?, LONG_OBJECT_STATE=?, CS_WAITMODE=?, MIN_NUMB_OF_RESP=?, NUMB_OF_WAITS=?, TIMEOUT=? WHERE ID=?");
			try {
				for (BatchCommand<Executor, Command> _cmd : commands) {
					Command cmd = (Command)_cmd;
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
					SerializedWorkflow sw = cmd.serializer.serializeWorkflow(rc.workflow);
					updateWfiStmt.setInt(idx++, DBProcessingState.WAITING.ordinal());
					updateWfiStmt.setInt(idx++, rc.workflow.getPriority());
					updateWfiStmt.setTimestamp(idx++, now);
					updateWfiStmt.setString(idx++, rc.workflow.getProcessorPoolId());
					if (sw.getData() != null) {
						updateWfiStmt.setString(idx++, sw.getData().length() > 4000 ? null : sw.getData());
						updateWfiStmt.setString(idx++, sw.getData().length() > 4000 ? sw.getData() : null);
					}
					else {
						updateWfiStmt.setString(idx++, null);
						updateWfiStmt.setString(idx++, null);
					}
					if (sw.getObjectState() != null) {
						updateWfiStmt.setString(idx++, sw.getObjectState().length() > 4000 ? null : sw.getObjectState());
						updateWfiStmt.setString(idx++, sw.getObjectState().length() > 4000 ? sw.getObjectState() : null);
					}
					else {
						updateWfiStmt.setString(idx++, null);
						updateWfiStmt.setString(idx++, null);
					}
					updateWfiStmt.setInt(idx++, rc.waitMode.ordinal());
					updateWfiStmt.setInt(idx++, rc.waitMode == WaitMode.FIRST ? 1 : rc.correlationIds.length);
					updateWfiStmt.setInt(idx++, rc.correlationIds.length);
					updateWfiStmt.setTimestamp(idx++, rc.timeoutTS);
					updateWfiStmt.setString(idx++, rc.workflow.getId());
					updateWfiStmt.addBatch();

					stmtDelQueue.setString(1, ((PersistentWorkflow<?>)rc.workflow).rowid);
					stmtDelQueue.setString(2, ((PersistentWorkflow<?>)rc.workflow).oldProcessorPoolId);
					stmtDelQueue.setInt(3, ((PersistentWorkflow<?>)rc.workflow).oldPrio);
					stmtDelQueue.addBatch();

					List<String> cidList = ((PersistentWorkflow<?>)rc.workflow).waitCidList;
					if (cidList != null) {
						for (String cid : cidList) {
							deleteWait.setString(1, cid);
							deleteWait.addBatch();
							doWaitDeletes = true;
						}
					}
					List<String> responseCidList = ((PersistentWorkflow<?>)rc.workflow).responseCidList;
					if (responseCidList != null) {
						for (String cid : responseCidList) {
							deleteResponse.setString(1, cid);
							deleteResponse.addBatch();
							doResponseDeletes = true;
						}
					}
				}
				if (doResponseDeletes) deleteResponse.executeBatch();
				if (doWaitDeletes) deleteWait.executeBatch();

				insertWaitStmt.executeBatch();
				updateWfiStmt.executeBatch();
				stmtDelQueue.executeBatch();


				for (BatchCommand<Executor, Command> _cmd : commands) {
					Command cmd = (Command)_cmd;
					RegisterCall rc = cmd.registerCall;
					for (WaitHook wh : rc.waitHooks) {
						wh.onWait(rc.workflow, con);
					}
				}
			}
			finally {
				JdbcUtils.closeStatement(stmtDelQueue);
				JdbcUtils.closeStatement(deleteWait);
				JdbcUtils.closeStatement(deleteResponse);
				JdbcUtils.closeStatement(insertWaitStmt);
				JdbcUtils.closeStatement(updateWfiStmt);
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
