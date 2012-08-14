package de.scoopgmbh.copper.persistent.txn;

public interface TransactionController {
	public <T> T run(final Transactional<T> txn) throws Exception;
}
