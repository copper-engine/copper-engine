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

import org.apache.log4j.Logger;

import de.scoopgmbh.copper.Workflow;

/**
 * MySQL implementation of the {@link ScottyDBStorageInterface}.
 * 
 * @author austermann
 *
 */
public class MySqlScottyDBStorage extends AbstractSqlScottyDBStorage implements ScottyDBStorageInterface {
	
	private static final Logger logger = Logger.getLogger(MySqlScottyDBStorage.class);

	protected PreparedStatement createUpdateStateStmt(final Connection c, final int max) throws SQLException {
		final Timestamp NOW = new Timestamp(System.currentTimeMillis());
		PreparedStatement pstmt = c.prepareStatement(queryUpdateQueueState+" LIMIT 0,"+max);
		pstmt.setTimestamp(1, NOW);
		pstmt.setTimestamp(2, NOW);
		return pstmt;
	}

	protected PreparedStatement createDequeueStmt(final Connection c, final String ppoolId, final int max) throws SQLException {
		PreparedStatement dequeueStmt = c.prepareStatement("select id,priority,data,creation_ts from COP_WORKFLOW_INSTANCE where id in (select WORKFLOW_INSTANCE_ID from cop_queue where ppool_id = ? order by priority, last_mod_ts) LIMIT 0,"+max);
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
		getBatcher().submitBatchCommand(new MySqlSetToError.Command(pwf,getDataSource(),t));
	}
}
