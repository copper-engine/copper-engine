package de.scoopgmbh.copper.persistent.txn;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import de.scoopgmbh.copper.spring.SpringTransaction;

public class SpringTransactionController implements TransactionController {

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
	public <T> T run(final Transactional<T> txn) throws Exception {
		final T[] t = (T[]) new Object[1];
		new SpringTransaction() {
			@Override
			protected void execute(Connection con) throws Exception {
				t[0] = txn.run(con);
			}
		}.run(transactionManager, dataSource, new DefaultTransactionDefinition());
		return t[0];
	}

}
