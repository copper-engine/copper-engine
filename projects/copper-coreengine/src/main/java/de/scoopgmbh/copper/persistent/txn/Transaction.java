package de.scoopgmbh.copper.persistent.txn;


/**
 * Defines a sequence of code that is executed in the scope of a transaction. The Transaction object is responsible to acquire ressources/connection
 * that are needed within this transaction.  
 * 
 * @author austermann
 *
 */
public interface Transaction<T> {

	public T run() throws Exception;

}
