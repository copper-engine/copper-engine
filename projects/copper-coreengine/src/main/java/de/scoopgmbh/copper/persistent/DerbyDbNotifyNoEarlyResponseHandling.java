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
package de.scoopgmbh.copper.persistent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.JdbcUtils;

import de.scoopgmbh.copper.Acknowledge;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.batcher.AbstractBatchCommand;
import de.scoopgmbh.copper.batcher.AcknowledgeCallbackWrapper;
import de.scoopgmbh.copper.batcher.BatchCommand;
import de.scoopgmbh.copper.batcher.BatchExecutor;

class DerbyDbNotifyNoEarlyResponseHandling {

	private static final Logger logger = LoggerFactory.getLogger(DerbyDbNotifyNoEarlyResponseHandling.class);

	static final class Command extends AbstractBatchCommand<Executor, Command>{

		final Response<?> response;
		final Serializer serializer;
		final int defaultStaleResponseRemovalTimeout;

		public Command(Response<?> response, Serializer serializer, int defaultStaleResponseRemovalTimeout, final long targetTime, Acknowledge ack) {
			super(new AcknowledgeCallbackWrapper<Command>(ack),targetTime);
			this.response = response;
			this.serializer = serializer;
			this.defaultStaleResponseRemovalTimeout = defaultStaleResponseRemovalTimeout;
		}

		@Override
		public Executor executor() {
			return Executor.INSTANCE;
		}

	}

	static final class Executor extends BatchExecutor<Executor, Command>{

		private static final Executor INSTANCE = new Executor();

		@Override
		public int maximumBatchSize() {
			return 100;
		}

		@Override
		public int preferredBatchSize() {
			return 50;
		}

		@Override
		public void doExec(final Collection<BatchCommand<Executor, Command>> commands, final Connection con) throws Exception {
			if (commands.isEmpty())
				return;

			final PreparedStatement selectStmt = con.prepareStatement("select count(*) from cop_wait where correlation_id = ?");
			final PreparedStatement insertStmt = con.prepareStatement("INSERT INTO cop_response (CORRELATION_ID, RESPONSE_TS, RESPONSE, RESPONSE_TIMEOUT, RESPONSE_META_DATA, RESPONSE_ID) VALUES (?,?,?,?,?,?)");
			try {
				final Timestamp now = new Timestamp(System.currentTimeMillis());
				int counter = 0;
				for (BatchCommand<Executor, Command> _cmd : commands) {
					Command cmd = (Command)_cmd;
					selectStmt.clearParameters();
					selectStmt.setString(1, cmd.response.getCorrelationId());
					ResultSet rs = selectStmt.executeQuery();
					rs.next();
					final int c = rs.getInt(1);
					rs.close();

					if (c == 1) {
						insertStmt.setString(1, cmd.response.getCorrelationId());
						insertStmt.setString(2, cmd.response.getCorrelationId());
						insertStmt.setTimestamp(3, now);
						final String payload = cmd.serializer.serializeResponse(cmd.response);
						insertStmt.setString(4, payload);
						insertStmt.setTimestamp(5, new Timestamp(System.currentTimeMillis() + (cmd.response.getInternalProcessingTimeout() == null ? cmd.defaultStaleResponseRemovalTimeout : cmd.response.getInternalProcessingTimeout())));
						insertStmt.setString(6, cmd.response.getMetaData());
						insertStmt.setString(7, cmd.response.getResponseId());
						insertStmt.addBatch();
						counter++;
					}
				}
				if (counter > 0) {
					insertStmt.executeBatch();
				}
			}
			catch(SQLException e) {
				logger.error("doExec failed",e);
				logger.error("NextException=",e.getNextException());
				throw e;
			}
			catch(Exception e) {
				logger.error("doExec failed",e);
				throw e;
			}
			finally {
				JdbcUtils.closeStatement(insertStmt);
			}
		}

	}

}
