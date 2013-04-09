package de.scoopgmbh.copper.persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource40;
import org.junit.Before;
import org.junit.Test;

import de.scoopgmbh.copper.InterruptException;
import de.scoopgmbh.copper.audit.BatchingAuditTrail;
import de.scoopgmbh.copper.batcher.impl.BatcherImpl;
import de.scoopgmbh.copper.db.utility.RetryingTransaction;
import de.scoopgmbh.copper.instrument.Transformed;
import de.scoopgmbh.copper.monitor.adapter.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowStateSummary;
import de.scoopgmbh.copper.monitor.adapter.model.WorkflowSummary;


public class DerbyDbDialectTest{
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
	
	EmbeddedConnectionPoolDataSource40 datasource_default;
	private DerbyDbDialect derbyDbDialect;	
	@Before
	public void setUp(){
		datasource_default = new EmbeddedConnectionPoolDataSource40();
		datasource_default.setDatabaseName("./build/copperExampleDB;create=true");
		

		derbyDbDialect = new DerbyDbDialect();
		derbyDbDialect.setDataSource(datasource_default);
		derbyDbDialect.startup();
		try {
			this.cleanDB(datasource_default);
			DerbyDbDialect.checkAndCreateSchema(datasource_default);//cleandb
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Test
	public void test_selectTotalWorkflowSummary() throws SQLException, Exception{
		DummyPersistentWorkflow wf = new DummyPersistentWorkflow("id", "ppoolId", "1", 1);
		derbyDbDialect.insert(wf, datasource_default.getConnection());
		
		try {
			WorkflowStateSummary selectedWorkflowStateSummary = derbyDbDialect.selectTotalWorkflowStateSummary(datasource_default.getConnection());
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
			List<AuditTrailInfo> selectAuditTrails = derbyDbDialect.selectAuditTrails(null, null, null, null, 3, datasource_default.getConnection());
			assertEquals(1, selectAuditTrails.size());
			assertEquals(occurrence.getTime(),selectAuditTrails.get(0).getOccurrence().getTime());
			assertEquals(1,selectAuditTrails.get(0).getLoglevel());
		} catch (SQLException e) {
			throw new RuntimeException(e);
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
			this.oldPrio = prio;
			this.oldProcessorPoolId = ppoolId;
			this.rowid = rowid;
		}

		@Override
		public void main() throws InterruptException {
		}

	}
	
	@Test
	public void test_selectWorkflowSummary() throws SQLException, Exception{
		{
			DummyPersistentWorkflow wf = new DummyPersistentWorkflow("id1", "P#DEFAULT", "1", 1);
			derbyDbDialect.insert(wf, datasource_default.getConnection());
		}
		{
			DummyPersistentWorkflow wf = new DummyPersistentWorkflow("id2", "P#DEFAULT", "2", 1);
			derbyDbDialect.insert(wf, datasource_default.getConnection());
		}
		{
			DummyPersistentWorkflow2 wf = new DummyPersistentWorkflow2("id3", "P#DEFAULT", "3", 1);
			derbyDbDialect.insert(wf, datasource_default.getConnection());
		}
		
		try {
			List<WorkflowSummary> selectSummary = derbyDbDialect.selectWorkflowStateSummary(null, null, datasource_default.getConnection());
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
			DummyPersistentWorkflow wf = new DummyPersistentWorkflow("id1", "P#DEFAULT", "1", 1);
			derbyDbDialect.insert(wf, datasource_default.getConnection());
		}
		{
			DummyPersistentWorkflow wf = new DummyPersistentWorkflow("id2", "P#DEFAULT", "2", 1);
			derbyDbDialect.insert(wf, datasource_default.getConnection());
		}
		{
			DummyPersistentWorkflow2 wf = new DummyPersistentWorkflow2("id3", "P#DEFAULT", "3", 1);
			derbyDbDialect.insert(wf, datasource_default.getConnection());
		}
		
		try {
			List<WorkflowInstanceInfo> selectInstances = derbyDbDialect.selectWorkflowInstanceList(null, null, null, null, 1000, datasource_default.getConnection());
			assertEquals(3, selectInstances.size());
//			assertEquals(2,selectSummary.get(0).getStateSummary().getCount(WorkflowInstanceState.ENQUEUED));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}
	

}
