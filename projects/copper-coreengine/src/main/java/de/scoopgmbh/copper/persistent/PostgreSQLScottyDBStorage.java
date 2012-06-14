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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.Workflow;

/**
 * PostgreSQL implementation of the {@link ScottyDBStorageInterface}.
 * 
 * @author austermann
 *
 */
public class PostgreSQLScottyDBStorage extends AbstractSqlScottyDBStorage implements ScottyDBStorageInterface {
	
	private static final Logger logger = LoggerFactory.getLogger(PostgreSQLScottyDBStorage.class);

	protected PreparedStatement createUpdateStateStmt(final Connection c, final int max) throws SQLException {
		final Timestamp NOW = new Timestamp(System.currentTimeMillis());
		PreparedStatement pstmt = c.prepareStatement(queryUpdateQueueState+" LIMIT "+max);
		pstmt.setTimestamp(1, NOW);
		pstmt.setTimestamp(2, NOW);
		return pstmt;
	}

	protected PreparedStatement createDequeueStmt(final Connection c, final String ppoolId, final int max) throws SQLException {
		PreparedStatement dequeueStmt = c.prepareStatement("select id,priority,data,creation_ts from COP_WORKFLOW_INSTANCE where id in (select WORKFLOW_INSTANCE_ID from cop_queue where ppool_id = ? order by priority, last_mod_ts) LIMIT "+max);
		dequeueStmt.setString(1, ppoolId);
		return dequeueStmt;
	}

	protected PreparedStatement createDeleteStaleResponsesStmt(final Connection c, final int MAX_ROWS) throws SQLException {
		PreparedStatement stmt = c.prepareStatement("delete from cop_response where response_ts < ? and not exists (select * from cop_wait w where w.correlation_id = cop_response.correlation_id LIMIT "+MAX_ROWS+")");
		stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
		return stmt;
	}
	
	@Override
	public void error(Workflow<?> w, Throwable t) {
		if (logger.isTraceEnabled()) logger.trace("error("+w.getId()+","+t.toString()+")");
		final PersistentWorkflow<?> pwf = (PersistentWorkflow<?>) w;
		getBatcher().submitBatchCommand(new MySqlSetToError.Command(pwf,t));
	}
	
	@Override
	public void notify(List<Response<?>> responses, Connection c) throws Exception {
		if (responses.isEmpty()) 
			return;
		
		final PreparedStatement stmt = c.prepareCall("INSERT INTO COP_RESPONSE (CORRELATION_ID, RESPONSE_TS, RESPONSE) VALUES (?,?,?)");
		try {
			final Timestamp now = new Timestamp(System.currentTimeMillis());
			int counter=0;
			for(Response<?> r : responses) {
				stmt.setString(1, r.getCorrelationId());
				stmt.setTimestamp(2, now);
				String payload = serializer.serializeResponse(r);
				stmt.setString(3, payload);
				stmt.addBatch();
				counter++;
				if (counter == 50) {
					stmt.executeBatch();
					stmt.clearBatch();
					counter = 0;
				}
			}
			if (counter != 0) {
				stmt.executeBatch();
			}
		}
		finally {
			try {
				stmt.close();
			}
			catch(Exception e) {
				logger.error("stmt.close() failed",e);
			}
		}
	}	
}
