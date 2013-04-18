package de.scoopgmbh.copper.monitor.server.persistent;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;

import de.scoopgmbh.copper.audit.MessagePostProcessor;
import de.scoopgmbh.copper.batcher.Batcher;
import de.scoopgmbh.copper.monitor.core.adapter.model.AuditTrailInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.StorageInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.WorkflowInstanceInfo;
import de.scoopgmbh.copper.monitor.core.adapter.model.WorkflowInstanceState;
import de.scoopgmbh.copper.monitor.core.adapter.model.WorkflowStateSummary;
import de.scoopgmbh.copper.monitor.core.adapter.model.WorkflowSummary;
import de.scoopgmbh.copper.persistent.DatabaseDialect;
import de.scoopgmbh.copper.persistent.txn.DatabaseTransaction;
import de.scoopgmbh.copper.persistent.txn.TransactionController;

public class MonitoringDbStorage {

	private DatabaseMonitoringDialect dialect;
	private TransactionController transactionController;

	private Batcher batcher;
	private long deleteStaleResponsesIntervalMsec = 60L*60L*1000L;
	private int defaultStaleResponseRemovalTimeout = 60*60*1000;

	private Thread enqueueThread;
	private ScheduledExecutorService scheduledExecutorService;
	private boolean shutdown = false;
	private boolean checkDbConsistencyAtStartup = false;
	
	private CountDownLatch enqueueThreadTerminated = new CountDownLatch(1);

	public MonitoringDbStorage() {

	}

	public void setCheckDbConsistencyAtStartup(boolean checkDbConsistencyAtStartup) {
		this.checkDbConsistencyAtStartup = checkDbConsistencyAtStartup;
	}

	public void setTransactionController(TransactionController transactionController) {
		this.transactionController = transactionController;
	}

	public void setDialect(DatabaseMonitoringDialect dialect) {
		this.dialect = dialect;
	}

	protected  <T> T run(final DatabaseTransaction<T> txn) throws Exception {
		return transactionController.run(txn);
	}

	/**
	 * Sets the default removal timeout for stale responses in the underlying database. A response is stale/timed out when
	 * there is no workflow instance waiting for it within the specified amount of time. 
	 * @param defaultStaleResponseRemovalTimeout
	 */
	public void setDefaultStaleResponseRemovalTimeout(int defaultStaleResponseRemovalTimeout) {
		this.defaultStaleResponseRemovalTimeout = defaultStaleResponseRemovalTimeout;
	}

	public void setBatcher(Batcher batcher) {
		this.batcher = batcher;
	}
	
	public Batcher getBatcher() {
		return this.batcher;
	}

	public void setDeleteStaleResponsesIntervalMsec(long deleteStaleResponsesIntervalMsec) {
		this.deleteStaleResponsesIntervalMsec = deleteStaleResponsesIntervalMsec;
	}

	public WorkflowStateSummary selectTotalWorkflowStateSummary() {
		try {
			return run(new DatabaseTransaction<WorkflowStateSummary>() {
				@Override
				public WorkflowStateSummary run(Connection con) throws Exception {
					return dialect.selectTotalWorkflowStateSummary(con);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<AuditTrailInfo> selectAuditTrails(final String workflowClass, final String workflowInstanceId, final String correlationId, final Integer level, final long resultRowLimit) {
		try {
			return run(new DatabaseTransaction<List<AuditTrailInfo>>() {
				@Override
				public List<AuditTrailInfo> run(Connection con) throws Exception {
					return dialect.selectAuditTrails(workflowClass, workflowInstanceId, correlationId, level, resultRowLimit, con);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String selectAuditTrailMessage(final long id,final MessagePostProcessor messagePostProcessor) {
		try {
			return run(new DatabaseTransaction<String>() {
				@Override
				public String run(Connection con) throws Exception {
					return dialect.selectAuditTrailMessage(id,con,messagePostProcessor);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<WorkflowSummary> selectWorkflowSummary(final String poolid, final String classname) {
		try {
			return run(new DatabaseTransaction<List<WorkflowSummary> >() {
				@Override
				public List<WorkflowSummary> run(Connection con) throws Exception {
					return dialect.selectWorkflowStateSummary(poolid, classname,con);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<WorkflowInstanceInfo> selectWorkflowInstanceList(final String poolid, final String classname,
			final WorkflowInstanceState state, final Integer priority, final long resultRowLimit) {
		try {
			return run(new DatabaseTransaction<List<WorkflowInstanceInfo>>() {
				@Override
				public List<WorkflowInstanceInfo> run(Connection con) throws Exception {
					return dialect.selectWorkflowInstanceList(poolid, classname, state, priority, resultRowLimit,con);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<String[]> executeMonitoringQuery(final String query, final long resultRowLimit) {
		try {
			return run(new DatabaseTransaction<List<String[]>>() {
				@Override
				public List<String[]> run(Connection con) throws Exception {
					return dialect.executeMonitoringQuery(query, resultRowLimit,con);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
