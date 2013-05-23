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
import java.sql.Timestamp;
import java.util.Collection;

import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.batcher.AbstractBatchCommand;
import de.scoopgmbh.copper.batcher.BatchCommand;
import de.scoopgmbh.copper.batcher.BatchExecutor;
import de.scoopgmbh.copper.batcher.NullCallback;

class SqlNotify {

	static final class Command extends AbstractBatchCommand<Executor, Command>{

		final Response<?> response;
		final Serializer serializer;
		final int defaultStaleResponseRemovalTimeout;

		@SuppressWarnings("unchecked")
		public Command(Response<?> response, Serializer serializer, int defaultStaleResponseRemovalTimeout, final long targetTime) {
			super(NullCallback.instance,targetTime);
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
			final Timestamp now = new Timestamp(System.currentTimeMillis());
			final PreparedStatement stmt = con.prepareStatement("INSERT INTO COP_RESPONSE (CORRELATION_ID, RESPONSE_TS, RESPONSE, RESPONSE_TIMEOUT, RESPONSE_META_DATA, RESPONSE_ID) VALUES (?,?,?,?,?,?)");
			for (BatchCommand<Executor, Command> _cmd : commands) {
				Command cmd = (Command)_cmd;
				stmt.setString(1, cmd.response.getCorrelationId());
				stmt.setTimestamp(2, now);
				String payload = cmd.serializer.serializeResponse(cmd.response);
				stmt.setString(3, payload);
				stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis() + (cmd.response.getInternalProcessingTimeout() == null ? cmd.defaultStaleResponseRemovalTimeout : cmd.response.getInternalProcessingTimeout())));
				stmt.setString(5, cmd.response.getMetaData());
				stmt.setString(6, cmd.response.getResponseId());
				stmt.addBatch();
			}
			stmt.executeBatch();
		}

	}

}
