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
package de.scoopgmbh.copper.audit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource40;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.scoopgmbh.copper.audit.BatchingAuditTrail.Property2ColumnMapping;
import de.scoopgmbh.copper.persistent.DerbyDbDialect;

public class BatchingAuditTrailTest extends TestCase {
	
	private static final Logger logger = LoggerFactory.getLogger(BatchingAuditTrailTest.class);

	EmbeddedConnectionPoolDataSource40 ds;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ds = new EmbeddedConnectionPoolDataSource40();
		ds.setDatabaseName("./target/copperUnitTestDB;create=true");
		DerbyDbDialect.checkAndCreateSchema(ds);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		DerbyDbDialect.shutdownDerby();
	}

	public void testGetSqlStmt() throws Exception {
		BatchingAuditTrail batchingAuditTrail = new BatchingAuditTrail();
		batchingAuditTrail.setDataSource(ds);
		batchingAuditTrail.startup();

		Assert.assertEquals("INSERT INTO COP_AUDIT_TRAIL_EVENT (LOGLEVEL,OCCURRENCE,CONVERSATION_ID,CONTEXT,INSTANCE_ID,CORRELATION_ID,TRANSACTION_ID,MESSAGE_TYPE,LONG_MESSAGE) VALUES (?,?,?,?,?,?,?,?,?)", batchingAuditTrail.getSqlStmt());
	}


	public void testLog() throws Exception {
		BatchingAuditTrail batchingAuditTrail = new BatchingAuditTrail();
		batchingAuditTrail.setDataSource(ds);
		batchingAuditTrail.startup();

		Connection con = ds.getConnection();
		try {
			con.createStatement().execute("DELETE FROM COP_AUDIT_TRAIL_EVENT");

			AuditTrailEvent e = new AuditTrailEvent(1, new Date(), "conversationId", "context", "instanceId", "correlationId", "transactionId", "message", "messageType", null);
			batchingAuditTrail.doSyncLog(e , con);
			con.commit();
		}
		finally {
			con.close();
		}

		Assert.assertEquals("INSERT INTO COP_AUDIT_TRAIL_EVENT (LOGLEVEL,OCCURRENCE,CONVERSATION_ID,CONTEXT,INSTANCE_ID,CORRELATION_ID,TRANSACTION_ID,MESSAGE_TYPE,LONG_MESSAGE) VALUES (?,?,?,?,?,?,?,?,?)", batchingAuditTrail.getSqlStmt());
	}	

	public void testCustomTable() throws Exception {
		createCustomAuditTrailTable();

		final ArrayList<Property2ColumnMapping> additionalMapping = new ArrayList<BatchingAuditTrail.Property2ColumnMapping>();
		additionalMapping.add(new Property2ColumnMapping("customInt", "CUSTOM_INT"));
		additionalMapping.add(new Property2ColumnMapping("customTimestamp", "CUSTOM_TIMESTAMP"));
		additionalMapping.add(new Property2ColumnMapping("customVarchar", "CUSTOM_VARCHAR"));
		
		final BatchingAuditTrail batchingAuditTrail = new BatchingAuditTrail();
		batchingAuditTrail.setDataSource(ds);
		batchingAuditTrail.setDbTable("COP_AUDIT_TRAIL_EVENT_EXTENDED");
		batchingAuditTrail.setAuditTrailEventClass(ExtendedAutitTrailEvent.class);
		batchingAuditTrail.setAdditionalMapping(additionalMapping);		
		batchingAuditTrail.startup();

		Assert.assertEquals("INSERT INTO COP_AUDIT_TRAIL_EVENT_EXTENDED (CUSTOM_INT,CUSTOM_TIMESTAMP,CUSTOM_VARCHAR,LOGLEVEL,OCCURRENCE,CONVERSATION_ID,CONTEXT,INSTANCE_ID,CORRELATION_ID,TRANSACTION_ID,MESSAGE_TYPE,LONG_MESSAGE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)", batchingAuditTrail.getSqlStmt());
		
		final Connection con = ds.getConnection();
		try {
			con.createStatement().execute("DELETE FROM COP_AUDIT_TRAIL_EVENT_EXTENDED");
			
			ExtendedAutitTrailEvent e = new ExtendedAutitTrailEvent(1, new Date(), "conversationId", "context", "instanceId", "correlationId", "transactionId", "message", "messageType", "TEST", 4711, new Timestamp(System.currentTimeMillis()));
			batchingAuditTrail.doSyncLog(e , con);
			con.commit();
			
			AuditTrailEvent e2 = new AuditTrailEvent(1, new Date(), "conversationId", "context", "instanceId", "correlationId", "transactionId", "message", "messageType", null);
			batchingAuditTrail.doSyncLog(e2 , con);
			con.commit();

			ResultSet rs = con.createStatement().executeQuery("SELECT * FROM COP_AUDIT_TRAIL_EVENT_EXTENDED ORDER BY SEQ_ID ASC");
			Assert.assertTrue(rs.next());
			Assert.assertEquals("conversationId", rs.getString("CONVERSATION_ID"));
			Assert.assertEquals("TEST", rs.getString("CUSTOM_VARCHAR"));
			
			Assert.assertTrue(rs.next());
			Assert.assertEquals("conversationId", rs.getString("CONVERSATION_ID"));
			Assert.assertNull(rs.getString("CUSTOM_VARCHAR"));
			
			Assert.assertFalse(rs.next());
		}
		finally {
			con.close();
		}

	}

	private void createCustomAuditTrailTable() throws IOException, SQLException {
		final StringBuilder sql = new StringBuilder();
		final BufferedReader r = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("extendedAuditTrail.sql")));
		try {
			String l = null;
			while ((l=r.readLine()) != null) {
				sql.append(l).append("\n");
			}
		}
		finally {
			r.close();
		}
		
		final String sqlStmt = sql.toString();
		final Connection c = ds.getConnection();
		try {
			c.createStatement().execute(sqlStmt);
		}
		catch(SQLException e) {
			logger.debug("creation of table failed",e);
		}
		finally {
			c.close();
		}
	}	

}
