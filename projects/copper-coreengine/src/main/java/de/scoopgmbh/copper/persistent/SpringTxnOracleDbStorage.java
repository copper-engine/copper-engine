package de.scoopgmbh.copper.persistent;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import de.scoopgmbh.copper.CopperRuntimeException;
import de.scoopgmbh.copper.Response;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.batcher.BatchCommand;

abstract class Transaction {
	abstract void execute(Connection con) throws Exception;
	void run(PlatformTransactionManager transactionManager, DataSource dataSource, TransactionDefinition def) {
		TransactionStatus txnStatus = transactionManager.getTransaction(def);
		try {
			Connection con = DataSourceUtils.getConnection(dataSource);
			try {
				execute(con);
			}
			finally {
				DataSourceUtils.releaseConnection(con, dataSource);
			}
		}
		catch(RuntimeException e) {
			transactionManager.rollback(txnStatus);
			throw e;
		}
		catch(Exception e) {
			transactionManager.rollback(txnStatus);
			throw new CopperRuntimeException("jdbc operation failed",e);
		}
		transactionManager.commit(txnStatus);
	}
}

public class SpringTxnOracleDbStorage extends OracleScottyDBStorage {
	
	private static final Logger logger = LoggerFactory.getLogger(SpringTxnOracleDbStorage.class);

	private PlatformTransactionManager transactionManager;
	
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
	private void runTransactional(BatchCommand<?, ?> cmd) {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		TransactionStatus txnStatus = transactionManager.getTransaction(def);
		try {
			doTransactional(getDataSource(), cmd);
		}
		catch(RuntimeException e) {
			transactionManager.rollback(txnStatus);
			throw e;
		}
		catch(Exception e) {
			transactionManager.rollback(txnStatus);
			throw new CopperRuntimeException("jdbc operation failed",e);
		}
		transactionManager.commit(txnStatus);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void doTransactional(DataSource ds, BatchCommand<?, ?> cmd) throws Exception {
		Connection con = DataSourceUtils.getConnection(ds);
		try {
			cmd.executor().doExec((Collection)Collections.singletonList(cmd), con);
		}
		finally {
			DataSourceUtils.releaseConnection(con, ds);
		}
	}
	
	
	@Override
	public void finish(Workflow<?> w) {
		if (logger.isTraceEnabled()) logger.trace("finish("+w.getId()+")");
		final PersistentWorkflow<?> pwf = (PersistentWorkflow<?>) w;
		runTransactional(new GenericRemove.Command(pwf,isRemoveWhenFinished()));
	}

	@Override
	public void error(Workflow<?> w, Throwable t) {
		if (logger.isTraceEnabled()) logger.trace("error("+w.getId()+")");
		final PersistentWorkflow<?> pwf = (PersistentWorkflow<?>) w;
		runTransactional(new GenericSetToError.Command(pwf,t));
	}
	
	@Override
	public void registerCallback(RegisterCall rc) throws Exception {
		if (logger.isTraceEnabled()) logger.trace("registerCallback("+rc+")");
		if (rc == null) throw new NullPointerException();
		runTransactional(new GenericRegisterCallback.Command(rc, getDataSource(), getSerializer() , null));
	}
	
	@Override
	public void insert(final List<Workflow<?>> wfs) throws Exception {
		logger.trace("insert(wfs.size=?)", wfs.size());
		new Transaction() {
			@Override
			void execute(Connection con) throws Exception {
				doInsert(wfs, con);
			}
		}.run(transactionManager, getDataSource(), new DefaultTransactionDefinition());
	}
	
	@Override
	public void insert(final Workflow<?> wf) throws Exception {
		logger.trace("insert(?)", wf);
		new Transaction() {
			@Override
			void execute(Connection con) throws Exception {
				doInsert(wf, con);
			}
		}.run(transactionManager, getDataSource(), new DefaultTransactionDefinition());
	}
	
	@Override
	public void notify(final Response<?> response, Object callback) throws Exception {
		logger.trace("notify(?)", response);
		if (response == null) throw new NullPointerException();
		runTransactional(new GenericNotify.Command(response, getSerializer(), getDefaultStaleResponseRemovalTimeout()));
	}
	
	@Override
	public void notify(List<Response<?>> response) throws Exception {
		logger.trace("notify(response.size=?)", response.size());

		if (response.size() == 0) 
			return;
		
		final List<GenericNotify.Command> cmds = new ArrayList<GenericNotify.Command>(response.size());
		for (Response<?> r : response) {
			GenericNotify.Command cmd = new GenericNotify.Command(r, getSerializer(), getDefaultStaleResponseRemovalTimeout());
			cmds.add(cmd);
		}
		
		new Transaction() {
			@Override
			void execute(Connection con) throws Exception {
				cmds.get(0).executor().doExec((Collection)cmds, con);
			}
		}.run(transactionManager, getDataSource(), new DefaultTransactionDefinition());
	}
	
	
}
