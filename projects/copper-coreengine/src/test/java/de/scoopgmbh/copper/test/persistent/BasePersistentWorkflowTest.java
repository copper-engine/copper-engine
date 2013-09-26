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
package de.scoopgmbh.copper.test.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.scoopgmbh.copper.EngineState;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.WorkflowDescription;
import de.scoopgmbh.copper.WorkflowFactory;
import de.scoopgmbh.copper.WorkflowInstanceDescr;
import de.scoopgmbh.copper.audit.AuditTrailEvent;
import de.scoopgmbh.copper.audit.BatchingAuditTrail;
import de.scoopgmbh.copper.audit.CompressedBase64PostProcessor;
import de.scoopgmbh.copper.audit.DummyPostProcessor;
import de.scoopgmbh.copper.db.utility.RetryingTransaction;
import de.scoopgmbh.copper.persistent.PersistentScottyEngine;
import de.scoopgmbh.copper.persistent.ScottyDBStorageInterface;
import de.scoopgmbh.copper.test.backchannel.BackChannelQueue;
import de.scoopgmbh.copper.test.backchannel.WorkflowResult;

@WorkflowDescription(alias="de.scoopgmbh.copper.test.persistent.BasePersistentWorkflowTest", majorVersion=1, minorVersion=2, patchLevelVersion=3)
public class BasePersistentWorkflowTest {

	private static final Logger logger = LoggerFactory.getLogger(BasePersistentWorkflowTest.class);

	static final String PersistentUnitTestWorkflow_NAME = "de.scoopgmbh.copper.test.persistent.PersistentUnitTestWorkflow";

	public final void testDummy() {
		// for junit only
	}
	
	protected boolean skipTests() {
		return false;
	}

	void cleanDB(DataSource ds) throws Exception {
		new RetryingTransaction<Void>(ds) {
			@Override
			protected Void execute() throws Exception {
				Statement stmt = createStatement(getConnection());
				stmt.execute("DELETE FROM COP_AUDIT_TRAIL_EVENT");
				stmt.close();
				stmt = createStatement(getConnection());
				stmt.execute("DELETE FROM COP_WAIT");
				stmt.close();
				stmt = createStatement(getConnection());
				stmt.execute("DELETE FROM COP_RESPONSE");
				stmt.close();
				stmt = createStatement(getConnection());
				stmt.execute("DELETE FROM COP_QUEUE");
				stmt.close();
				stmt = createStatement(getConnection());
				stmt.execute("DELETE FROM COP_WORKFLOW_INSTANCE");
				stmt.close();
				stmt = createStatement(getConnection());
				stmt.execute("DELETE FROM COP_WORKFLOW_INSTANCE_ERROR");
				stmt.close();
				return null;
			}
		}.run();
	}
	
	private Statement createStatement(Connection con) throws SQLException {
		return con.createStatement(
		ResultSet.TYPE_SCROLL_INSENSITIVE,
		ResultSet.CONCUR_READ_ONLY,
		ResultSet.CLOSE_CURSORS_AT_COMMIT);
	}

	final String createTestData(int length) {
		StringBuilder dataSB = new StringBuilder(length);
		for (int i=0; i<length; i++) {
			int pos = (int)(Math.random()*70.0);
			dataSB.append("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890!§$%&/()=?".substring(pos,pos+1));
		}
		return dataSB.toString(); 
	}


	public void testAsnychResponse(String dsContext) throws Exception {
		assumeFalse(skipTests());
		logger.info("running testAsnychResponse");
		final int NUMB = 50;
		final String DATA = createTestData(50);
		final ConfigurableApplicationContext context = createContext(dsContext);
		cleanDB(context.getBean(DataSource.class));
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		engine.startup();
		final BackChannelQueue backChannelQueue = context.getBean(BackChannelQueue.class);
		try {
			assertEquals(EngineState.STARTED,engine.getEngineState());

			for (int i=0; i<NUMB; i++) {
				engine.run(PersistentUnitTestWorkflow_NAME, DATA);
			}

			for (int i=0; i<NUMB; i++) {
				WorkflowResult x = backChannelQueue.dequeue(60, TimeUnit.SECONDS);
				assertNotNull(x);
				assertNotNull(x.getResult());
				assertNotNull(x.getResult().toString().length() == DATA.length());
				assertNull(x.getException());
			}
		}
		finally {
			closeContext(context);
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());

	}

	protected void closeContext(final ConfigurableApplicationContext context) {
		context.close();
	}

	public void testAsnychResponseLargeData(String dsContext, int dataSize) throws Exception {
		assumeFalse(skipTests());
		logger.info("running testAsnychResponse");
		final int NUMB = 20;
		final String DATA = createTestData(dataSize);
		final ConfigurableApplicationContext context = createContext(dsContext);
		cleanDB(context.getBean(DataSource.class));
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		engine.startup();
		final BackChannelQueue backChannelQueue = context.getBean(BackChannelQueue.class);
		try {
			assertEquals(EngineState.STARTED,engine.getEngineState());

			for (int i=0; i<NUMB; i++) {
				engine.run(PersistentUnitTestWorkflow_NAME, DATA);
			}

			for (int i=0; i<NUMB; i++) {
				WorkflowResult x = backChannelQueue.dequeue(60, TimeUnit.SECONDS);
				assertNotNull(x);
				assertNotNull(x.getResult());
				assertNotNull(x.getResult().toString().length() == DATA.length());
				assertNull(x.getException());
			}
		}
		finally {
			closeContext(context);
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());

	}

	protected ConfigurableApplicationContext createContext(String dsContext) {
		//Thread.interrupted();
		final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {dsContext, "/CopperTxnPersistentWorkflowTest/persistent-engine-unittest-context.xml"});
		return context;
	}


	public void testWithConnection(String dsContext) throws Exception {
		assumeFalse(skipTests());
		logger.info("running testWithConnection");
		final int NUMB = 20;
		final ConfigurableApplicationContext context = createContext(dsContext);
		final DataSource ds = context.getBean(DataSource.class);
		cleanDB(ds);
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		engine.startup();
		final BackChannelQueue backChannelQueue = context.getBean(BackChannelQueue.class);
		try {
			assertEquals(EngineState.STARTED,engine.getEngineState());

			new RetryingTransaction<Void>(ds) {
				@Override
				protected Void execute() throws Exception {
					for (int i=0; i<NUMB; i++) {
						engine.run("de.scoopgmbh.copper.test.persistent.DBMockAdapterUsingPersistentUnitTestWorkflow", null);
					}
					return null;
				}
			}.run();

			for (int i=0; i<NUMB; i++) {
				WorkflowResult x = backChannelQueue.dequeue(60, TimeUnit.SECONDS);
				assertNotNull(x);
				assertNull(x.getResult());
				assertNull(x.getException());
			}
		}
		finally {
			closeContext(context);
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());

	}


	public void testWithConnectionBulkInsert(String dsContext) throws Exception {
		assumeFalse(skipTests());
		logger.info("running testWithConnectionBulkInsert");
		final int NUMB = 50;
		final ConfigurableApplicationContext context = createContext(dsContext);
		final DataSource ds = context.getBean(DataSource.class);
		cleanDB(ds);
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		engine.startup();
		final BackChannelQueue backChannelQueue = context.getBean(BackChannelQueue.class);
		try {
			assertEquals(EngineState.STARTED,engine.getEngineState());

			final List<Workflow<?>> list = new ArrayList<Workflow<?>>();
			for (int i=0; i<NUMB; i++) {
				WorkflowFactory<?> wfFactory = engine.createWorkflowFactory(PersistentUnitTestWorkflow_NAME);
				Workflow<?> wf = wfFactory.newInstance();
				list.add(wf);
			}

			new RetryingTransaction<Void>(ds) {
				@Override
				protected Void execute() throws Exception {
					engine.run(list,getConnection());
					return null;
				}
			}.run();

			for (int i=0; i<NUMB; i++) {
				WorkflowResult x = backChannelQueue.dequeue(60, TimeUnit.SECONDS);
				assertNotNull(x);
				assertNull(x.getResult());
				assertNull(x.getException());
			}
		}
		finally {
			closeContext(context);
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());

	}


	public void testTimeouts(String dsContext) throws Exception {
		assumeFalse(skipTests());
		logger.info("running testTimeouts");
		final int NUMB = 10;
		final ConfigurableApplicationContext context = createContext(dsContext);
		cleanDB(context.getBean(DataSource.class));
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		engine.startup();
		final BackChannelQueue backChannelQueue = context.getBean(BackChannelQueue.class);
		try {
			assertEquals(EngineState.STARTED,engine.getEngineState());

			for (int i=0; i<NUMB; i++) {
				engine.run("de.scoopgmbh.copper.test.persistent.TimingOutPersistentUnitTestWorkflow", null);
			}

			for (int i=0; i<NUMB; i++) {
				WorkflowResult x = backChannelQueue.dequeue(60, TimeUnit.SECONDS);
				assertNotNull(x);
				assertNull(x.getResult());
				assertNull(x.getException());
			}
		}
		finally {
			closeContext(context);
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());

	}


	public void testErrorHandlingInCoreEngine(String dsContext) throws Exception {
		assumeFalse(skipTests());
		final ConfigurableApplicationContext context = createContext(dsContext);
		cleanDB(context.getBean(DataSource.class));
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		try {
			engine.startup();
			final WorkflowInstanceDescr<Serializable> wfInstanceDescr = new WorkflowInstanceDescr<Serializable>("de.scoopgmbh.copper.test.persistent.ExceptionThrowingPersistentUnitTestWorkflow");
			wfInstanceDescr.setId(engine.createUUID());
			engine.run(wfInstanceDescr);
			Thread.sleep(5000);
			//check
			new RetryingTransaction<Void>(context.getBean(DataSource.class)) {
				@Override
				protected Void execute() throws Exception {
					Statement stmt = createStatement(getConnection());
					ResultSet rs = stmt.executeQuery("select * from cop_workflow_instance_error");
					assertTrue(rs.next());
					assertEquals(wfInstanceDescr.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
					assertNotNull(rs.getString("EXCEPTION"));
					assertFalse(rs.next());
					rs.close();
					stmt.close();
					return null;
				}
			}.run();
			engine.restart(wfInstanceDescr.getId());
			Thread.sleep(5000);
			new RetryingTransaction<Void>(context.getBean(DataSource.class)) {
				@Override
				protected Void execute() throws Exception {
					Statement stmt = createStatement(getConnection());
					ResultSet rs = stmt.executeQuery("select * from cop_workflow_instance_error");
					assertTrue(rs.next());
					assertEquals(wfInstanceDescr.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
					assertNotNull(rs.getString("EXCEPTION"));
					assertTrue(rs.next());
					assertEquals(wfInstanceDescr.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
					assertNotNull(rs.getString("EXCEPTION"));
					assertFalse(rs.next());
					rs.close();
					stmt.close();
					return null;
				}
			}.run();
		}
		finally {
			closeContext(context);
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());
	}


	public void testErrorHandlingInCoreEngine_restartAll(String dsContext) throws Exception {
		assumeFalse(skipTests());
		final ConfigurableApplicationContext context = createContext(dsContext);
		cleanDB(context.getBean(DataSource.class));
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		try {
			engine.startup();
			final WorkflowInstanceDescr<Serializable> wfInstanceDescr = new WorkflowInstanceDescr<Serializable>("de.scoopgmbh.copper.test.persistent.ExceptionThrowingPersistentUnitTestWorkflow");
			wfInstanceDescr.setId(engine.createUUID());
			engine.run(wfInstanceDescr);
			Thread.sleep(5000);
			//check
			new RetryingTransaction<Void>(context.getBean(DataSource.class)) {
				@Override
				protected Void execute() throws Exception {
					Statement stmt = createStatement(getConnection());
					ResultSet rs = stmt.executeQuery("select * from cop_workflow_instance_error");
					assertTrue(rs.next());
					assertEquals(wfInstanceDescr.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
					assertNotNull(rs.getString("EXCEPTION"));
					assertFalse(rs.next());
					rs.close();
					stmt.close();
					return null;
				}
			}.run();
			engine.restartAll();
			Thread.sleep(5000);
			new RetryingTransaction<Void>(context.getBean(DataSource.class)) {
				@Override
				protected Void execute() throws Exception {
					Statement stmt = createStatement(getConnection());
					ResultSet rs = stmt.executeQuery("select * from cop_workflow_instance_error");
					assertTrue(rs.next());
					assertEquals(wfInstanceDescr.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
					assertNotNull(rs.getString("EXCEPTION"));
					assertTrue(rs.next());
					assertEquals(wfInstanceDescr.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
					assertNotNull(rs.getString("EXCEPTION"));
					assertFalse(rs.next());
					rs.close();
					stmt.close();
					return null;
				}
			}.run();
		}
		finally {
			closeContext(context);
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());
	}


	public void testParentChildWorkflow(String dsContext) throws Exception {
		assumeFalse(skipTests());
		logger.info("running testParentChildWorkflow");
		final int NUMB = 20;
		final ConfigurableApplicationContext context = createContext(dsContext);
		cleanDB(context.getBean(DataSource.class));
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		engine.startup();
		final BackChannelQueue backChannelQueue = context.getBean(BackChannelQueue.class);
		try {
			assertEquals(EngineState.STARTED,engine.getEngineState());

			for (int i=0; i<NUMB; i++) {
				engine.run("de.scoopgmbh.copper.test.persistent.subworkflow.TestParentWorkflow",null);
			}

			for (int i=0; i<NUMB; i++) {
				WorkflowResult x = backChannelQueue.dequeue(60, TimeUnit.SECONDS);
				assertNotNull(x);
				assertNull(x.getResult());
				assertNull(x.getException());
			}
		}
		finally {
			closeContext(context);
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());
	}

	public void testErrorKeepWorkflowInstanceInDB(String dsContext) throws Exception {
		assumeFalse(skipTests());
		logger.info("running testErrorKeepWorkflowInstanceInDB");
		final int NUMB = 20;
		final String DATA = createTestData(50);
		final ConfigurableApplicationContext context = createContext(dsContext);
		cleanDB(context.getBean(DataSource.class));
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		final ScottyDBStorageInterface dbStorageInterface = context.getBean(ScottyDBStorageInterface.class);
		dbStorageInterface.setRemoveWhenFinished(false);
		engine.startup();
		final BackChannelQueue backChannelQueue = context.getBean(BackChannelQueue.class);
		try {
			assertEquals(EngineState.STARTED,engine.getEngineState());

			for (int i=0; i<NUMB; i++) {
				engine.run(PersistentUnitTestWorkflow_NAME, DATA);
			}

			for (int i=0; i<NUMB; i++) {
				WorkflowResult x = backChannelQueue.dequeue(60, TimeUnit.SECONDS);
				assertNotNull(x);
				assertNotNull(x.getResult());
				assertNotNull(x.getResult().toString().length() == DATA.length());
				assertNull(x.getException());
			}

			new RetryingTransaction<Void>(context.getBean(DataSource.class)) {
				@Override
				protected Void execute() throws Exception {
					Statement stmt = createStatement(getConnection());
					ResultSet rs = stmt.executeQuery("select count(*) from cop_workflow_instance");
					assertTrue(rs.next());
					int x = rs.getInt(1);
					assertEquals(NUMB, x);
					rs.close();
					stmt.close();
					return null;
				}
			}.run();

		}
		finally {
			closeContext(context);
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());
	}


	public void testCompressedAuditTrail(String dsContext) throws Exception {
		assumeFalse(skipTests());
		logger.info("running testCompressedAuditTrail");
		final int NUMB = 20;
		final String DATA = createTestData(50);
		final ConfigurableApplicationContext context = createContext(dsContext);
		context.getBean(BatchingAuditTrail.class).setMessagePostProcessor(new CompressedBase64PostProcessor());
		cleanDB(context.getBean(DataSource.class));
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		engine.startup();
		final BackChannelQueue backChannelQueue = context.getBean(BackChannelQueue.class);
		try {
			assertEquals(EngineState.STARTED,engine.getEngineState());

			for (int i=0; i<NUMB; i++) {
				engine.run(PersistentUnitTestWorkflow_NAME, DATA);
			}

			for (int i=0; i<NUMB; i++) {
				WorkflowResult x = backChannelQueue.dequeue(60, TimeUnit.SECONDS);
				assertNotNull(x);
				assertNotNull(x.getResult());
				assertNotNull(x.getResult().toString().length() == DATA.length());
				assertNull(x.getException());
			}
			Thread.sleep(1000);
			
			new RetryingTransaction<Void>(context.getBean(DataSource.class)) {
				@Override
				protected Void execute() throws Exception {
					Statement stmt = createStatement(getConnection());
					ResultSet rs = stmt.executeQuery("select unique message from (select dbms_lob.substr(long_message, 4000, 1 ) message from cop_audit_trail_event) order by 1 asc");
					assertTrue(rs.next());
					//logger.info("\""+new CompressedBase64PostProcessor().deserialize(rs.getString(1))+"\"");
					//System.out.println(new CompressedBase64PostProcessor().deserialize(rs.getString(1)));
					assertEquals("finished", new CompressedBase64PostProcessor().deserialize(rs.getString(1)));
					assertTrue(rs.next());
					assertEquals("foo successfully called", new CompressedBase64PostProcessor().deserialize(rs.getString(1)));
					//System.out.println(new CompressedBase64PostProcessor().deserialize(rs.getString(1)));
					assertFalse(rs.next());
					rs.close();
					stmt.close();
					return null;
				}
			}.run();

		}
		catch(Exception e) {
			logger.error("testCompressedAuditTrail failed",e);
			throw e;
		}
		finally {
			closeContext(context);
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());

	}



	public void testAutoCommit(String dsContext) throws Exception {
		assumeFalse(skipTests());
		logger.info("running testAutoCommit");
		final ConfigurableApplicationContext context = createContext(dsContext);
		try {
			DataSource ds = context.getBean(DataSource.class);
			new RetryingTransaction<Void>(ds) {
				@Override
				protected Void execute() throws Exception {
					assertFalse(getConnection().getAutoCommit());
					return null;
				}
			};
		}
		finally {
			closeContext(context);
		}
	}

	private static String createTestMessage(int size) {
		final StringBuilder sb = new StringBuilder(4000);
		for (int i=0; i<(size/10); i++) {
			sb.append("0123456789");
		}
		final String msg = sb.toString();
		return msg;
	}

	public void testAuditTrailUncompressed(String dsContext) throws Exception {
		assumeFalse(skipTests());
		logger.info("running testAuditTrailSmallData");
		final ConfigurableApplicationContext context = createContext(dsContext);
		try {
			de.scoopgmbh.copper.audit.BatchingAuditTrail auditTrail = context.getBean(de.scoopgmbh.copper.audit.BatchingAuditTrail.class);
			auditTrail.setMessagePostProcessor(new DummyPostProcessor());
			auditTrail.synchLog(1, new Date(), "4711", dsContext, "4711", "4711", "4711", null, "TEXT");
			auditTrail.synchLog(1, new Date(), "4711", dsContext, "4711", "4711", "4711", createTestMessage(500), "TEXT");
			auditTrail.synchLog(1, new Date(), "4711", dsContext, "4711", "4711", "4711", createTestMessage(5000), "TEXT");
			auditTrail.synchLog(1, new Date(), "4711", dsContext, "4711", "4711", "4711", createTestMessage(50000), "TEXT");
		}
		finally {
			closeContext(context);
		}
	}

	public void testErrorHandlingWithWaitHook(String dsContext) throws Exception {
		assumeFalse(skipTests());
		final ConfigurableApplicationContext context = createContext(dsContext);
		cleanDB(context.getBean(DataSource.class));
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		try {
			engine.startup();
			final WorkflowInstanceDescr<Serializable> wfInstanceDescr = new WorkflowInstanceDescr<Serializable>("de.scoopgmbh.copper.test.persistent.ErrorWaitHookUnitTestWorkflow");
			wfInstanceDescr.setId(engine.createUUID());
			engine.run(wfInstanceDescr, null);
			Thread.sleep(2500);
			//check
			new RetryingTransaction<Void>(context.getBean(DataSource.class)) {
				@Override
				protected Void execute() throws Exception {
					Statement stmt = createStatement(getConnection());
					ResultSet rs = stmt.executeQuery("select * from cop_workflow_instance_error");
					assertTrue(rs.next());
					assertEquals(wfInstanceDescr.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
					assertNotNull(rs.getString("EXCEPTION"));
					assertFalse(rs.next());
					rs.close();
					stmt.close();
					return null;
				}
			}.run();
		}
		finally {
			closeContext(context);
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());
	}

	public void testAuditTrailCustomSeqNr(String dsContext) throws Exception {
		assumeFalse(skipTests());
		logger.info("running testAuditTrailCustomSeqNr");
		final ConfigurableApplicationContext context = createContext(dsContext);
		try {
			cleanDB(context.getBean(DataSource.class));
			de.scoopgmbh.copper.audit.BatchingAuditTrail auditTrail = context.getBean(de.scoopgmbh.copper.audit.BatchingAuditTrail.class);
			auditTrail.setMessagePostProcessor(new DummyPostProcessor());
			long seqNr = 1;
			auditTrail.synchLog(new AuditTrailEvent(1, new Date(), "4711", dsContext, "4711", "4711", "4711", null, "TEXT", seqNr++));
			auditTrail.synchLog(new AuditTrailEvent(1, new Date(), "4711", dsContext, "4711", "4711", "4711", createTestMessage(500), "TEXT", seqNr++));
			auditTrail.synchLog(new AuditTrailEvent(1, new Date(), "4711", dsContext, "4711", "4711", "4711", createTestMessage(5000), "TEXT", seqNr++));
			auditTrail.synchLog(new AuditTrailEvent(1, new Date(), "4711", dsContext, "4711", "4711", "4711", createTestMessage(50000), "TEXT", seqNr++));
			//check
			new RetryingTransaction<Void>(context.getBean(DataSource.class)) {
				@Override
				protected Void execute() throws Exception {
					Statement stmt = createStatement(getConnection());
					ResultSet rs = stmt.executeQuery("select seq_id from cop_audit_trail_event order by seq_id");
					assertTrue(rs.next());
					assertEquals(1, rs.getLong(1));
					assertTrue(rs.next());
					assertEquals(2, rs.getLong(1));
					assertTrue(rs.next());
					assertEquals(3, rs.getLong(1));
					assertTrue(rs.next());
					assertEquals(4, rs.getLong(1));
					assertFalse(rs.next());
					rs.close();
					stmt.close();
					return null;
				}
			}.run();
		}
		finally {
			closeContext(context);
		}
	}

	public void testNotifyWithoutEarlyResponseHandling(String dsContext) throws Exception {
		assumeFalse(skipTests());
		logger.info("running testNotifyWithoutEarlyResponseHandling");
		final ConfigurableApplicationContext context = createContext(dsContext);
		cleanDB(context.getBean(DataSource.class));
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		try {
			engine.startup();
			new RetryingTransaction<Void>(context.getBean(DataSource.class)) {
				@Override
				protected Void execute() throws Exception {
					try {
						Response<?> response = new Response<String>("CID#withEarlyResponse", "TEST", null);
						engine.notify(response, getConnection());
						Statement stmt = createStatement(getConnection());
						ResultSet rs = stmt.executeQuery("select * from cop_response");
						assertTrue(rs.next());
						assertEquals(response.getCorrelationId(), rs.getString("CORRELATION_ID"));
						assertFalse(rs.next());
						getConnection().rollback();

						response = new Response<String>("CID#withoutEarlyResponse", "TEST", null);
						response.setEarlyResponseHandling(false);
						engine.notify(response, getConnection());
						rs = stmt.executeQuery("select * from cop_response");
						assertFalse(rs.next());
						rs.close();
						stmt.close();
						getConnection().rollback();
					}
					catch(Exception e) {
						logger.error("testNotifyWithoutEarlyResponseHandling failed",e);
						throw e;
					}
					return null;
				}
			}.run();
		}
		finally {
			closeContext(context);
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());

	}		
}
