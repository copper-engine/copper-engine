package de.scoopgmbh.copper.persistent.txn;

import java.sql.Connection;

/**
 * Defines a sequence of code that is executed in the scope of a transaction. A database connection is aquired by the executing 
 * transaction controller and passed DatabaseTransaction.
 * 
 * @author austermann
 *
 */
public interface DatabaseTransaction<T> {
	
	public T run(Connection con) throws Exception;

}
