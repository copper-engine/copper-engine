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
package de.scoopgmbh.copper.audit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.batcher.AbstractBatchCommand;
import de.scoopgmbh.copper.batcher.BatchCommand;
import de.scoopgmbh.copper.batcher.BatchExecutor;
import de.scoopgmbh.copper.batcher.CommandCallback;
import de.scoopgmbh.copper.batcher.NullCallback;

class BatchInsertIntoAutoTrail {

	static final class Command extends AbstractBatchCommand<Executor, Command>{

		final AuditTrailEvent data;

		@SuppressWarnings("unchecked")
		public Command(AuditTrailEvent data) {
			super(NullCallback.instance,250);
			this.data = data;
		}

		public Command(AuditTrailEvent data, CommandCallback<Command> callback) {
			super(callback,250);
			this.data = data;
		}

		@Override
		public Executor executor() {
			return Executor.INSTANCE;
		}

	}

	static final class Executor extends BatchExecutor<Executor, Command>{

		private static final Executor INSTANCE = new Executor();
		private static final Logger logger = LoggerFactory.getLogger(Executor.class);

		@Override
		public int maximumBatchSize() {
			return 50;
		}

		@Override
		public int preferredBatchSize() {
			return 20;
		}

		@Override
		public void doExec(final Collection<BatchCommand<Executor, Command>> commands, final Connection con) throws Exception {
			if (logger.isDebugEnabled()) logger.debug("DatabaseProductName="+con.getMetaData().getDatabaseProductName());
			String _stmt;
			final boolean isOracle = con.getMetaData().getDatabaseProductName().equalsIgnoreCase("oracle");
			if (isOracle) {
				// Oracle
				_stmt = "INSERT INTO COP_AUDIT_TRAIL_EVENT (SEQ_ID,OCCURRENCE,CONVERSATION_ID,LOGLEVEL,CONTEXT,INSTANCE_ID,CORRELATION_ID,TRANSACTION_ID, MESSAGE_TYPE, LONG_MESSAGE) VALUES (NVL(?,COP_SEQ_AUDIT_TRAIL.NEXTVAL),?,?,?,?,?,?,?,?,?)";
			}
			else {
				// ANSI SQL
				_stmt = "INSERT INTO COP_AUDIT_TRAIL_EVENT (OCCURRENCE,CONVERSATION_ID,LOGLEVEL,CONTEXT,INSTANCE_ID,CORRELATION_ID,LONG_MESSAGE,TRANSACTION_ID,MESSAGE_TYPE) VALUES (?,?,?,?,?,?,?,?,?)";
				
			}
			final PreparedStatement stmt = con.prepareStatement(_stmt);
			for (BatchCommand<Executor, Command> _cmd : commands) {
				Command cmd = (Command)_cmd;
				int idx=1;
				AuditTrailEvent data = cmd.data;
				if (isOracle) {
					if (data.getSequenceId() == null) {
						stmt.setNull(idx++, Types.NUMERIC);
					}
					else {
						stmt.setLong(idx++, data.getSequenceId().longValue());
					}
					stmt.setTimestamp(idx++, new Timestamp(data.occurrence.getTime()));
					stmt.setString(idx++, data.conversationId);
					stmt.setInt(idx++, data.logLevel);
					stmt.setString(idx++, data.context);
					stmt.setString(idx++, data.instanceId);
					stmt.setString(idx++, data.correlationId);
					stmt.setString(idx++, data.transactionId);
					stmt.setString(idx++, data.messageType);
					stmt.setString(idx++, data.message);
				}
				else {
					if (data.getSequenceId() != null) {
						throw new UnsupportedOperationException("Custom SequenceId currently not supported for this DBMS");
					}
					stmt.setTimestamp(idx++, new Timestamp(data.occurrence.getTime()));
					stmt.setString(idx++, data.conversationId);
					stmt.setInt(idx++, data.logLevel);
					stmt.setString(idx++, data.context);
					stmt.setString(idx++, data.instanceId);
					stmt.setString(idx++, data.correlationId);
					stmt.setString(idx++, data.message);
					stmt.setString(idx++, data.transactionId);
					stmt.setString(idx++, data.messageType);
				}
				stmt.addBatch();
			}
			stmt.executeBatch();
		}

	}

}
