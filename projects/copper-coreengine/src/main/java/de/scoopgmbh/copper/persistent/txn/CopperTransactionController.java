package de.scoopgmbh.copper.persistent.txn;

import javax.sql.DataSource;

import de.scoopgmbh.copper.db.utility.RetryingTransaction;

/**
 * Implementation of the {@link TransactionController} interface that internally uses COPPERs {@link RetryingTransaction} for transaction management
 *  
 * @author austermann
 *
 */
public class CopperTransactionController implements TransactionController {
	
	private DataSource dataSource;
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T run(final DatabaseTransaction<T> txn) throws Exception {
		final T[] t = (T[]) new Object[1];
		new RetryingTransaction(dataSource) {
			@Override
			protected void execute() throws Exception {
				t[0] = txn.run(getConnection());
			}
		}.run();
		return t[0];
	}

	@Override
	public <T> T run(Transaction<T> txn) throws Exception {
		return txn.run();
	}
}
