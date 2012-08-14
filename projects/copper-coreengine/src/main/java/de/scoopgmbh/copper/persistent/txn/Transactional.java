package de.scoopgmbh.copper.persistent.txn;

import java.sql.Connection;

public interface Transactional<T> {
	
	public T run(Connection con) throws Exception;

}
