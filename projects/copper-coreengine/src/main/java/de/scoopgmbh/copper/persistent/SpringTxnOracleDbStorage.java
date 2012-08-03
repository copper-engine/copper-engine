package de.scoopgmbh.copper.persistent;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import de.scoopgmbh.copper.CopperRuntimeException;
import de.scoopgmbh.copper.Workflow;
import de.scoopgmbh.copper.batcher.BatchCommand;

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
}
