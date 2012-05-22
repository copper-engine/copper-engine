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
package de.scoopgmbh.copper.test.persistent;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.scoopgmbh.copper.EngineState;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.WorkflowFactory;
import de.scoopgmbh.copper.audit.AuditTrail;
import de.scoopgmbh.copper.audit.BatchingAuditTrail;
import de.scoopgmbh.copper.audit.CompressedBase64PostProcessor;
import de.scoopgmbh.copper.audit.DummyPostProcessor;
import de.scoopgmbh.copper.db.utility.RetryingTransaction;
import de.scoopgmbh.copper.persistent.PersistentScottyEngine;
import de.scoopgmbh.copper.persistent.ScottyDBStorageInterface;
import de.scoopgmbh.copper.test.backchannel.BackChannelQueue;
import de.scoopgmbh.copper.test.backchannel.WorkflowResult;

public class PersistentWorkflowTest extends TestCase {
	
	private static final Logger logger = LoggerFactory.getLogger(PersistentWorkflowTest.class);
	
	static final String PersistentUnitTestWorkflow_CLASS = "de.scoopgmbh.copper.test.persistent.PersistentUnitTestWorkflow";
	
	public final void testDummy() {
		// for junit only
	}
	
	void cleanDB(DataSource ds) throws Exception {
		new RetryingTransaction(ds) {
			@Override
			protected void execute() throws Exception {
				getConnection().createStatement().execute("DELETE FROM COP_AUDIT_TRAIL_EVENT");
				getConnection().createStatement().execute("DELETE FROM COP_WAIT");
				getConnection().createStatement().execute("DELETE FROM COP_RESPONSE");
				getConnection().createStatement().execute("DELETE FROM COP_QUEUE");
				getConnection().createStatement().execute("DELETE FROM COP_WORKFLOW_INSTANCE");
				getConnection().createStatement().execute("DELETE FROM COP_WORKFLOW_INSTANCE_ERROR");
			}
		}.run();
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
		logger.info("running testAsnychResponse");
		final int NUMB = 20;
		final String DATA = createTestData(50);
		final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {dsContext, "persistent-engine-unittest-context.xml", "unittest-context.xml"});
		cleanDB(context.getBean(DataSource.class));
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		engine.startup();
		final BackChannelQueue backChannelQueue = context.getBean(BackChannelQueue.class);
		try {
			assertEquals(EngineState.STARTED,engine.getEngineState());
			
			for (int i=0; i<NUMB; i++) {
				engine.run(PersistentUnitTestWorkflow_CLASS, DATA);
			}

			for (int i=0; i<NUMB; i++) {
				WorkflowResult x = backChannelQueue.dequeue(60, TimeUnit.SECONDS);
				Assert.assertNotNull(x);
				Assert.assertNotNull(x.getResult());
				Assert.assertNotNull(x.getResult().toString().length() == DATA.length());
				Assert.assertNull(x.getException());
			}
		}
		finally {
			context.close();
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());
		
	}

	public void testAsnychResponseLargeData(String dsContext, int dataSize) throws Exception {
		logger.info("running testAsnychResponse");
		final int NUMB = 20;
		final String DATA = createTestData(dataSize);
		final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {dsContext, "persistent-engine-unittest-context.xml", "unittest-context.xml"});
		cleanDB(context.getBean(DataSource.class));
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		engine.startup();
		final BackChannelQueue backChannelQueue = context.getBean(BackChannelQueue.class);
		try {
			assertEquals(EngineState.STARTED,engine.getEngineState());
			
			for (int i=0; i<NUMB; i++) {
				WorkflowFactory<String> wfFactory = engine.createWorkflowFactory(PersistentUnitTestWorkflow_CLASS);
				Workflow<String> wf = wfFactory.newInstance();
				wf.setData(DATA);
				engine.run(wf);
			}

			for (int i=0; i<NUMB; i++) {
				WorkflowResult x = backChannelQueue.dequeue(60, TimeUnit.SECONDS);
				Assert.assertNotNull(x);
				Assert.assertNotNull(x.getResult());
				Assert.assertNotNull(x.getResult().toString().length() == DATA.length());
				Assert.assertNull(x.getException());
			}
		}
		finally {
			context.close();
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());

	}

	public void testWithConnection(String dsContext) throws Exception {
		logger.info("running testWithConnection");
		final int NUMB = 20;
		final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {dsContext, "persistent-engine-unittest-context.xml", "unittest-context.xml"});
		final DataSource ds = context.getBean(DataSource.class);
		cleanDB(ds);
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		engine.startup();
		final BackChannelQueue backChannelQueue = context.getBean(BackChannelQueue.class);
		try {
			assertEquals(EngineState.STARTED,engine.getEngineState());

			new RetryingTransaction(ds) {
				@Override
				protected void execute() throws Exception {
					for (int i=0; i<NUMB; i++) {
						engine.run("de.scoopgmbh.copper.test.persistent.DBMockAdapterUsingPersistentUnitTestWorkflow", null);
					}
				}
			}.run();

			for (int i=0; i<NUMB; i++) {
				WorkflowResult x = backChannelQueue.dequeue(60, TimeUnit.SECONDS);
				Assert.assertNotNull(x);
				Assert.assertNull(x.getResult());
				Assert.assertNull(x.getException());
			}
		}
		finally {
			context.close();
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());
		
	}
	
	public void testWithConnectionBulkInsert(String dsContext) throws Exception {
		logger.info("running testWithConnectionBulkInsert");
		final int NUMB = 50;
		final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {dsContext, "persistent-engine-unittest-context.xml", "unittest-context.xml"});
		final DataSource ds = context.getBean(DataSource.class);
		cleanDB(ds);
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		engine.startup();
		final BackChannelQueue backChannelQueue = context.getBean(BackChannelQueue.class);
		try {
			assertEquals(EngineState.STARTED,engine.getEngineState());

			final List<Workflow<?>> list = new ArrayList<Workflow<?>>();
			for (int i=0; i<NUMB; i++) {
				WorkflowFactory<?> wfFactory = engine.createWorkflowFactory(PersistentUnitTestWorkflow_CLASS);
				Workflow<?> wf = wfFactory.newInstance();
				list.add(wf);
			}
			
			new RetryingTransaction(ds) {
				@Override
				protected void execute() throws Exception {
					engine.run(list,getConnection());
				}
			}.run();

			for (int i=0; i<NUMB; i++) {
				WorkflowResult x = backChannelQueue.dequeue(60, TimeUnit.SECONDS);
				Assert.assertNotNull(x);
				Assert.assertNull(x.getResult());
				Assert.assertNull(x.getException());
			}
		}
		finally {
			context.close();
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());
		
	}
	
	public void testTimeouts(String dsContext) throws Exception {
		logger.info("running testTimeouts");
		final int NUMB = 10;
		final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {dsContext, "persistent-engine-unittest-context.xml", "unittest-context.xml"});
		cleanDB(context.getBean(DataSource.class));
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		engine.startup();
		final BackChannelQueue backChannelQueue = context.getBean(BackChannelQueue.class);
		try {
			assertEquals(EngineState.STARTED,engine.getEngineState());
			
			for (int i=0; i<NUMB; i++) {
				WorkflowFactory<?> wfFactory = engine.createWorkflowFactory("de.scoopgmbh.copper.test.persistent.TimingOutPersistentUnitTestWorkflow");
				Workflow<?> wf = wfFactory.newInstance();
				engine.run(wf);
			}

			for (int i=0; i<NUMB; i++) {
				WorkflowResult x = backChannelQueue.dequeue(60, TimeUnit.SECONDS);
				Assert.assertNotNull(x);
				Assert.assertNull(x.getResult());
				Assert.assertNull(x.getException());
			}
		}
		finally {
			context.close();
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());
		
	}
	
	public void testErrorHandlingInCoreEngine(String dsContext) throws Exception {
		final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {dsContext, "persistent-engine-unittest-context.xml", "unittest-context.xml"});
		cleanDB(context.getBean(DataSource.class));
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		try {
			engine.startup();
			WorkflowFactory<?> wfFactory = engine.createWorkflowFactory("de.scoopgmbh.copper.test.persistent.ExceptionThrowingPersistentUnitTestWorkflow");
			final Workflow<?> wf = wfFactory.newInstance();
			engine.run(wf);
			Thread.sleep(5000);
			//check
			new RetryingTransaction(context.getBean(DataSource.class)) {
				@Override
				protected void execute() throws Exception {
					ResultSet rs = getConnection().createStatement().executeQuery("select * from cop_workflow_instance_error");
					assertTrue(rs.next());
					assertEquals(wf.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
					assertNotNull(rs.getString("EXCEPTION"));
					assertFalse(rs.next());
				}
			}.run();
			engine.restart(wf.getId());
			Thread.sleep(5000);
			new RetryingTransaction(context.getBean(DataSource.class)) {
				@Override
				protected void execute() throws Exception {
					ResultSet rs = getConnection().createStatement().executeQuery("select * from cop_workflow_instance_error");
					assertTrue(rs.next());
					assertEquals(wf.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
					assertNotNull(rs.getString("EXCEPTION"));
					assertTrue(rs.next());
					assertEquals(wf.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
					assertNotNull(rs.getString("EXCEPTION"));
					assertFalse(rs.next());
				}
			}.run();
		}
		finally {
			context.close();
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());
	}
	
	public void testErrorHandlingInCoreEngine_restartAll(String dsContext) throws Exception {
		final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {dsContext, "persistent-engine-unittest-context.xml", "unittest-context.xml"});
		cleanDB(context.getBean(DataSource.class));
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		try {
			engine.startup();
			WorkflowFactory<?> wfFactory = engine.createWorkflowFactory("de.scoopgmbh.copper.test.persistent.ExceptionThrowingPersistentUnitTestWorkflow");
			final Workflow<?> wf = wfFactory.newInstance();
			engine.run(wf);
			Thread.sleep(5000);
			//check
			new RetryingTransaction(context.getBean(DataSource.class)) {
				@Override
				protected void execute() throws Exception {
					ResultSet rs = getConnection().createStatement().executeQuery("select * from cop_workflow_instance_error");
					assertTrue(rs.next());
					assertEquals(wf.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
					assertNotNull(rs.getString("EXCEPTION"));
					assertFalse(rs.next());
				}
			}.run();
			engine.restartAll();
			Thread.sleep(5000);
			new RetryingTransaction(context.getBean(DataSource.class)) {
				@Override
				protected void execute() throws Exception {
					ResultSet rs = getConnection().createStatement().executeQuery("select * from cop_workflow_instance_error");
					assertTrue(rs.next());
					assertEquals(wf.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
					assertNotNull(rs.getString("EXCEPTION"));
					assertTrue(rs.next());
					assertEquals(wf.getId(), rs.getString("WORKFLOW_INSTANCE_ID"));
					assertNotNull(rs.getString("EXCEPTION"));
					assertFalse(rs.next());
				}
			}.run();
		}
		finally {
			context.close();
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());
	}
	
	
	public void testParentChildWorkflow(String dsContext) throws Exception {
		logger.info("running testParentChildWorkflow");
		final int NUMB = 20;
		final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {dsContext, "persistent-engine-unittest-context.xml", "unittest-context.xml"});
		cleanDB(context.getBean(DataSource.class));
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		engine.startup();
		final BackChannelQueue backChannelQueue = context.getBean(BackChannelQueue.class);
		try {
			assertEquals(EngineState.STARTED,engine.getEngineState());
			
			for (int i=0; i<NUMB; i++) {
				WorkflowFactory<?> wfFactory = engine.createWorkflowFactory("de.scoopgmbh.copper.test.persistent.subworkflow.TestParentWorkflow");
				Workflow<?> wf = wfFactory.newInstance();
				engine.run(wf);
			}

			for (int i=0; i<NUMB; i++) {
				WorkflowResult x = backChannelQueue.dequeue(60, TimeUnit.SECONDS);
				Assert.assertNotNull("Timeout!",x);
				Assert.assertNull(x.getResult());
				Assert.assertNull(x.getException());
			}
		}
		finally {
			context.close();
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());
	}
	
	
	public void testErrorKeepWorkflowInstanceInDB(String dsContext) throws Exception {
		logger.info("running testErrorKeepWorkflowInstanceInDB");
		final int NUMB = 20;
		final String DATA = createTestData(50);
		final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {dsContext, "persistent-engine-unittest-context.xml", "unittest-context.xml"});
		cleanDB(context.getBean(DataSource.class));
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		final ScottyDBStorageInterface dbStorageInterface = context.getBean(ScottyDBStorageInterface.class);
		dbStorageInterface.setRemoveWhenFinished(false);
		engine.startup();
		final BackChannelQueue backChannelQueue = context.getBean(BackChannelQueue.class);
		try {
			assertEquals(EngineState.STARTED,engine.getEngineState());
			
			for (int i=0; i<NUMB; i++) {
				WorkflowFactory<String> wfFactory = engine.createWorkflowFactory(PersistentUnitTestWorkflow_CLASS);
				Workflow<String> wf = wfFactory.newInstance();
				wf.setData(DATA);
				engine.run(wf);
			}

			for (int i=0; i<NUMB; i++) {
				WorkflowResult x = backChannelQueue.dequeue(60, TimeUnit.SECONDS);
				Assert.assertNotNull(x);
				Assert.assertNotNull(x.getResult());
				Assert.assertNotNull(x.getResult().toString().length() == DATA.length());
				Assert.assertNull(x.getException());
			}

			new RetryingTransaction(context.getBean(DataSource.class)) {
				@Override
				protected void execute() throws Exception {
					ResultSet rs = getConnection().createStatement().executeQuery("select count(*) from cop_workflow_instance");
					assertTrue(rs.next());
					int x = rs.getInt(1);
					assertEquals(NUMB, x);
				}
			}.run();
			
		}
		finally {
			context.close();
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());
	}
	
	public void testCompressedAuditTrail(String dsContext) throws Exception {
		logger.info("running testCompressedAuditTrail");
		final int NUMB = 20;
		final String DATA = createTestData(50);
		final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {dsContext, "persistent-engine-unittest-context.xml", "unittest-context.xml"});
		context.getBean(BatchingAuditTrail.class).setMessagePostProcessor(new CompressedBase64PostProcessor());
		cleanDB(context.getBean(DataSource.class));
		final PersistentScottyEngine engine = context.getBean(PersistentScottyEngine.class);
		engine.startup();
		final BackChannelQueue backChannelQueue = context.getBean(BackChannelQueue.class);
		try {
			assertEquals(EngineState.STARTED,engine.getEngineState());
			
			for (int i=0; i<NUMB; i++) {
				WorkflowFactory<String> wfFactory = engine.createWorkflowFactory("de.scoopgmbh.copper.test.persistent.PersistentUnitTestWorkflow");
				Workflow<String> wf = wfFactory.newInstance();
				wf.setData(DATA);
				engine.run(wf);
			}

			for (int i=0; i<NUMB; i++) {
				WorkflowResult x = backChannelQueue.dequeue(60, TimeUnit.SECONDS);
				Assert.assertNotNull(x);
				Assert.assertNotNull(x.getResult());
				Assert.assertNotNull(x.getResult().toString().length() == DATA.length());
				Assert.assertNull(x.getException());
			}
			new RetryingTransaction(context.getBean(DataSource.class)) {
				@Override
				protected void execute() throws Exception {
					ResultSet rs = getConnection().createStatement().executeQuery("select unique message from cop_audit_trail_event order by message");
					assertTrue(rs.next());
					//logger.info("\""+new CompressedBase64PostProcessor().deserialize(rs.getString(1))+"\"");
					assertEquals("finished", new CompressedBase64PostProcessor().deserialize(rs.getString(1)));
					assertTrue(rs.next());
					assertEquals("foo successfully called", new CompressedBase64PostProcessor().deserialize(rs.getString(1)));
					assertFalse(rs.next());
				}
			}.run();
			
		}
		finally {
			context.close();
		}
		assertEquals(EngineState.STOPPED,engine.getEngineState());
		assertEquals(0,engine.getNumberOfWorkflowInstances());
		
	}	
	
	public void testAutoCommit(String dsContext) throws Exception {
		logger.info("running testAutoCommit");
		final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {dsContext, "persistent-engine-unittest-context.xml", "unittest-context.xml"});
		try {
			DataSource ds = context.getBean(DataSource.class);
			new RetryingTransaction(ds) {
				@Override
				protected void execute() throws Exception {
					assertFalse(getConnection().getAutoCommit());
				}
			};
		}
		finally {
			context.close();
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
		logger.info("running testAuditTrailSmallData");
		final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(new String[] {dsContext, "persistent-engine-unittest-context.xml", "unittest-context.xml"});
		try {
			de.scoopgmbh.copper.audit.BatchingAuditTrail auditTrail = context.getBean(de.scoopgmbh.copper.audit.BatchingAuditTrail.class);
			auditTrail.setMessagePostProcessor(new DummyPostProcessor());
			auditTrail.synchLog(1, new Date(), "4711", dsContext, "4711", "4711", "4711", null, "TEXT");
			auditTrail.synchLog(1, new Date(), "4711", dsContext, "4711", "4711", "4711", createTestMessage(500), "TEXT");
			auditTrail.synchLog(1, new Date(), "4711", dsContext, "4711", "4711", "4711", createTestMessage(5000), "TEXT");
			auditTrail.synchLog(1, new Date(), "4711", dsContext, "4711", "4711", "4711", createTestMessage(50000), "TEXT");
		}
		finally {
			context.close();
		}
	}
	
}
