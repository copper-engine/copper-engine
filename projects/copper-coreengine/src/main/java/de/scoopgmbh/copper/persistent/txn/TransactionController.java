package de.scoopgmbh.copper.persistent.txn;

/**
 * COPPER supports custom Transaction Management by using a built-in or custom Transaction Controller.
 * COPPER comes with a simple build in Transaction Mgmt (see {@link CopperTransactionController} and
 * a transaction controller that uses Springs Transaction Managament (see {@link SpringTransactionController}).
 * 
 * @author austermann
 *
 */
public interface TransactionController {
	
	/**
	 * Runs a database transaction, i.e. a database connection is aquired and the provided DatabaseTransaction object is executed 
	 * in the scope of a transaction.
	 */
	public <T> T run(final DatabaseTransaction<T> txn) throws Exception;
	
	/**
	 * Runs a transaction, the provided DatabaseTransaction object is executed in the scope of a transaction.
	 * A database, JMS, or any other connection may be aquired later on in this transaction, but to do so is in the scope of the 
	 * Transaction object. The TransactionController is just defining the start and end of the transaction. 
	 */
	public <T> T run(final Transaction<T> txn) throws Exception;
	
}
