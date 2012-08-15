package de.scoopgmbh.copper.persistent.txn;

import java.sql.Connection;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import de.scoopgmbh.copper.spring.SpringTransaction;

/**
 * Implementation of the {@link TransactionController} interface that internally uses Springs Transaction Management
 *  
 * @author austermann
 *
 */
public class SpringTransactionController implements TransactionController {
	
	private static final Logger logger = LoggerFactory.getLogger(SpringTransactionController.class);

	private DataSource dataSource;
	private PlatformTransactionManager transactionManager;
	
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T run(final DatabaseTransaction<T> txn) throws Exception {
		final T[] t = (T[]) new Object[1];
		new SpringTransaction() {
			@Override
			protected void execute(Connection con) throws Exception {
				t[0] = txn.run(con);
			}
		}.run(transactionManager, dataSource, new DefaultTransactionDefinition());
		return t[0];
	}

	@Override
	public <T> T run(Transaction<T> txn) throws Exception {
		final TransactionStatus txnStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
		T t = null;
		try {
			t = txn.run();
		}
		catch(Exception e) {
			logger.error("execution failed - rolling back transaction",e);
			transactionManager.rollback(txnStatus);
			throw e;
		}
		transactionManager.commit(txnStatus);
		return t;
	}

}
