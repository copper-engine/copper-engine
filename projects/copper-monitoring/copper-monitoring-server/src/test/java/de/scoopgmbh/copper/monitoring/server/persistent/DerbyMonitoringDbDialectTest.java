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
package de.scoopgmbh.copper.monitoring.server.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource40;
import org.junit.Before;
import org.junit.Test;

import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.audit.BatchingAuditTrail;
import de.scoopgmbh.copper.batcher.impl.BatcherImpl;
import de.scoopgmbh.copper.instrument.Transformed;
import de.scoopgmbh.copper.monitoring.core.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitoring.core.model.MessageInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowStateSummary;
import de.scoopgmbh.copper.monitoring.core.model.WorkflowSummary;
import de.scoopgmbh.copper.persistent.DerbyDbDialect;
import de.scoopgmbh.copper.persistent.PersistentWorkflow;
import de.scoopgmbh.copper.persistent.StandardJavaSerializer;


public class DerbyMonitoringDbDialectTest {
	
	EmbeddedConnectionPoolDataSource40 datasource_default;
	private DerbyMonitoringDbDialect derbyMonitoringDbDialect;
	private DerbyDbDialect derbyDbDialect;
	@Before
	public void setUp(){
		datasource_default = new EmbeddedConnectionPoolDataSource40();
		datasource_default.setDatabaseName("./build/copperExampleDB;create=true");
		
		derbyMonitoringDbDialect = new DerbyMonitoringDbDialect(new StandardJavaSerializer());
		derbyDbDialect = new DerbyDbDialect();
		derbyDbDialect.setDataSource(datasource_default);
		derbyDbDialect.startup();

		Connection connection = null;
		try {
			connection = datasource_default.getConnection();
			connection.setAutoCommit(false);
			DerbyCleanDbUtil.dropSchema(connection.getMetaData(), "APP"); // APP = default schema
			DerbyDbDialect.checkAndCreateSchema(datasource_default); 
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}

	}
	
	@Test
	public void test_selectTotalWorkflowSummary() throws SQLException, Exception{
		DummyPersistentWorkflow1 wf = new DummyPersistentWorkflow1("id", "ppoolId", "1", 1);
		derbyDbDialect.insert(wf, datasource_default.getConnection());
		
		try {
			WorkflowStateSummary selectedWorkflowStateSummary = derbyMonitoringDbDialect.selectTotalWorkflowStateSummary(datasource_default.getConnection());
			assertTrue(selectedWorkflowStateSummary.getNumberOfWorkflowInstancesWithState().size()>0);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Test
	public void test_selectAudittrail() throws SQLException, Exception{

		BatcherImpl batcher = new BatcherImpl(3);
		BatchingAuditTrail auditTrail = new BatchingAuditTrail();
		auditTrail.setBatcher(batcher);
		auditTrail.setDataSource(datasource_default);
//		auditTrail.setMessagePostProcessor(new CompressedBase64PostProcessor());
		try {
			auditTrail.startup();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Date occurrence = new Date();
		auditTrail.synchLog(1, occurrence, "", "", "", "", "", "detail", "Text");
		
		try {
			List<AuditTrailInfo> selectAuditTrails = derbyMonitoringDbDialect.selectAuditTrails(null, null, null, null, 3, datasource_default.getConnection());
			assertEquals(1, selectAuditTrails.size());
			assertEquals(occurrence.getTime(),selectAuditTrails.get(0).getOccurrence().getTime());
			assertEquals(1,selectAuditTrails.get(0).getLoglevel());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Transformed
	public static class DummyPersistentWorkflow1 extends PersistentWorkflow<Serializable> {

		private static final long serialVersionUID = 7047352707643389609L;
		
		public DummyPersistentWorkflow1(String id, String ppoolId, String rowid, int prio) {
			if (id == null) throw new NullPointerException();
			if (ppoolId == null) throw new NullPointerException();
			setId(id);
			setProcessorPoolId(ppoolId);
			setPriority(prio);
		}

		@Override
		public void main() throws InterruptException {
		}
	}
	
	@Transformed
	public static class DummyPersistentWorkflow2 extends PersistentWorkflow<Serializable> {

		private static final long serialVersionUID = 7047352707643389609L;
		
		public DummyPersistentWorkflow2(String id, String ppoolId, String rowid, int prio) {
			if (id == null) throw new NullPointerException();
			if (ppoolId == null) throw new NullPointerException();
			setId(id);
			setProcessorPoolId(ppoolId);
			setPriority(prio);
		}

		@Override
		public void main() throws InterruptException {
		}
	}
	
	@Test
	public void test_selectWorkflowSummary() throws SQLException, Exception{
		{
			DummyPersistentWorkflow1 wf = new DummyPersistentWorkflow1("id1", "P#DEFAULT", "1", 1);
			derbyDbDialect.insert(wf, datasource_default.getConnection());
		}
		{
			DummyPersistentWorkflow1 wf = new DummyPersistentWorkflow1("id2", "P#DEFAULT", "2", 1);
			derbyDbDialect.insert(wf, datasource_default.getConnection());
		}
		{
			DummyPersistentWorkflow2 wf = new DummyPersistentWorkflow2("id3", "P#DEFAULT", "3", 1);
			derbyDbDialect.insert(wf, datasource_default.getConnection());
		}
		
		try {
			List<WorkflowSummary> selectSummary = derbyMonitoringDbDialect.selectWorkflowStateSummary(null, null, datasource_default.getConnection());
			assertEquals(2, selectSummary.size());
			assertEquals(1,selectSummary.get(0).getStateSummary().getCount(WorkflowInstanceState.ENQUEUED));
			assertEquals(2,selectSummary.get(1).getStateSummary().getCount(WorkflowInstanceState.ENQUEUED));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
	}

	
	@Test
	public void test_selectWorkflowinstance() throws SQLException, Exception{
		{
			DummyPersistentWorkflow1 wf = new DummyPersistentWorkflow1("id1", "P#DEFAULT", "1", 1);
			derbyDbDialect.insert(wf, datasource_default.getConnection());
		}
		{
			DummyPersistentWorkflow1 wf = new DummyPersistentWorkflow1("id2", "P#DEFAULT", "2", 1);
			derbyDbDialect.insert(wf, datasource_default.getConnection());
		}
		{
			DummyPersistentWorkflow2 wf = new DummyPersistentWorkflow2("id3", "P#DEFAULT", "3", 1);
			derbyDbDialect.insert(wf, datasource_default.getConnection());
		}
		
		try {
			List<WorkflowInstanceInfo> selectInstances = derbyMonitoringDbDialect.selectWorkflowInstanceList(null, null, null, null, null, null, 1000, datasource_default.getConnection());
			assertEquals(3, selectInstances.size());
//			assertEquals(2,selectSummary.get(0).getStateSummary().getCount(WorkflowInstanceState.ENQUEUED));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@Test
	public void test_selectWorkflowinstance_timeframe() throws SQLException, Exception{
		{
			DummyPersistentWorkflow1 wf = new DummyPersistentWorkflow1("id1", "P#DEFAULT", "1", 1);
			derbyDbDialect.insert(wf, datasource_default.getConnection());
		}
		{
			DummyPersistentWorkflow1 wf = new DummyPersistentWorkflow1("id2", "P#DEFAULT", "2", 1);
			derbyDbDialect.insert(wf, datasource_default.getConnection());
		}
		{
			DummyPersistentWorkflow2 wf = new DummyPersistentWorkflow2("id3", "P#DEFAULT", "3", 1);
			derbyDbDialect.insert(wf, datasource_default.getConnection());
		}
		
		try {
			{
				List<WorkflowInstanceInfo> selectInstances = derbyMonitoringDbDialect.selectWorkflowInstanceList(null, null, null, null, new Date(1), null, 1000, datasource_default.getConnection());
				assertEquals(3, selectInstances.size());
			}
			{
				List<WorkflowInstanceInfo> selectInstances = derbyMonitoringDbDialect.selectWorkflowInstanceList(null, null, null, null, null, new Date(System.currentTimeMillis()+10000), 1000, datasource_default.getConnection());
				assertEquals(3, selectInstances.size());
			}
//			assertEquals(2,selectSummary.get(0).getStateSummary().getCount(WorkflowInstanceState.ENQUEUED));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@Test
	public void test_selectMessages() throws SQLException, Exception{
		final Response<String> response = new Response<String>("test123");
		response.setResponseId("5456465");
		List<Response<?>> list = new ArrayList<Response<?>>();
		list.add(response);
		derbyDbDialect.notify(list, datasource_default.getConnection());
		
		try {
			List<MessageInfo> messages = derbyMonitoringDbDialect.selectMessages(false, 1000, datasource_default.getConnection());
			assertEquals(1, messages.size());
			
			List<MessageInfo> messages2 = derbyMonitoringDbDialect.selectMessages(true, 1000, datasource_default.getConnection());
			assertEquals(1, messages2.size());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
}
