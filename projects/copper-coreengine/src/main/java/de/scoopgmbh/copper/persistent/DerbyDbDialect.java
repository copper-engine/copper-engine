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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.batcher.BatchCommand;

/**
 * Apache Derby implementation of the {@link DatabaseDialect} interface.
 * 
 * @author austermann
 *
 */
public class DerbyDbDialect extends AbstractSqlDialect {

	private static final Logger logger = LoggerFactory.getLogger(DerbyDbDialect.class);
	
	private DataSource dataSource;
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@Override
	public synchronized void startup() {
		try {
			if (dataSource == null) throw new NullPointerException("dataSource in "+getClass().getSimpleName()+" is null");
			checkAndCreateSchema(dataSource);
		} 
		catch (Exception e) {
			throw new Error("startup failed",e);
		}
		super.startup();
	}

	@Override
	protected PreparedStatement createUpdateStateStmt(final Connection c, final int max) throws SQLException {
		final Timestamp NOW = new Timestamp(System.currentTimeMillis());
		PreparedStatement pstmt = c.prepareStatement(queryUpdateQueueState+" FETCH FIRST "+max+" ROWS ONLY");
		pstmt.setTimestamp(1, NOW);
		pstmt.setTimestamp(2, NOW);
		return pstmt;
	}

	@Override
	protected PreparedStatement createDequeueStmt(final Connection c, final String ppoolId, final int maxRows) throws SQLException {
		PreparedStatement dequeueStmt = c.prepareStatement("select id,priority,data,object_state,creation_ts from COP_WORKFLOW_INSTANCE where id in (select WORKFLOW_INSTANCE_ID from cop_queue where ppool_id = ? order by priority, last_mod_ts) FETCH FIRST "+maxRows+" ROWS ONLY");
		dequeueStmt.setString(1, ppoolId);
		return dequeueStmt;
	}

	@Override
	protected PreparedStatement createDeleteStaleResponsesStmt(final Connection c, final int maxRows) throws SQLException {
		PreparedStatement stmt = c.prepareStatement("delete from cop_response where response_timeout < ? and not exists (select * from cop_wait w where w.correlation_id = cop_response.correlation_id FETCH FIRST "+maxRows+" ROWS ONLY)");
		stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
		return stmt;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public BatchCommand createBatchCommand4error(Workflow<?> w, Throwable t, DBProcessingState dbProcessingState) {
		return new DerbyDbSetToError.Command((PersistentWorkflow<?>) w,t,dbProcessingState);
	}
		

	public static void checkAndCreateSchema(DataSource ds) throws SQLException, IOException {
		Connection c = ds.getConnection();
		try {
			if (tablesExist(c)) {
				logger.info("COPPER schema already exists");
				return;
			}
			logger.info("Creating COPPER schema...");
			BufferedReader reader = new BufferedReader(new InputStreamReader(DerbyDbDialect.class.getResourceAsStream("/derbydb/create-schema.sql")));
			try {
				String s;
				StringBuilder sb = new StringBuilder(256);
				while ((s = reader.readLine()) != null) {
					s = s.trim();
					if (s.length() == 0 || s.startsWith("--")) 
						continue;
					sb.append(s);
					if (s.endsWith(";")) {
						sb.deleteCharAt(sb.length()-1);
						logger.info("Executing: "+sb.toString());
						String sql = sb.toString();
						Statement stmt = c.createStatement();
						try {
							stmt.execute(sql);
						}
						catch(SQLException e) {
							logger.error("",e);
						}
						finally {
							stmt.close();
						}
						sb = new StringBuilder(256);
					}
					else {
						sb.append("\n");
					}
				}
			}
			finally {
				reader.close();
			}
		}
		finally {
			c.close();
		}
	}

	private static boolean tablesExist(Connection c) throws SQLException {
		final Statement stmt = c.createStatement();
		try {
			stmt.execute("SELECT count(*) FROM COP_WORKFLOW_INSTANCE");
			return true;
		}
		catch(SQLException e) {
			return false;
		}
		finally {
			stmt.close();
		}
	}

	public static void shutdownDerby() {
		boolean gotSQLExc = false;
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} 
		catch (SQLException se) {
			if ( se.getSQLState().equals("XJ015") ) {
				gotSQLExc = true;
			}
		}
		if (!gotSQLExc) {
			logger.warn("Database did not shut down normally");
		} 
		else {
			logger.info("Database shut down normally");
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public BatchCommand createBatchCommand4NotifyNoEarlyResponseHandling(Response<?> response) throws Exception {
		return new DerbyDbNotifyNoEarlyResponseHandling.Command(response, serializer, defaultStaleResponseRemovalTimeout, System.currentTimeMillis()+dbBatchingLatencyMSec);
	}	




}
	

