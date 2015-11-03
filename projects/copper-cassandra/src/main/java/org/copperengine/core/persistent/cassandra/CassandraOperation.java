package org.copperengine.core.persistent.cassandra;

import org.slf4j.Logger;

import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;

abstract class CassandraOperation<T> {

    private final Logger logger;

    public CassandraOperation(Logger logger) {
        this.logger = logger;
    }

    public T run() throws Exception {
        for (int i = 1;; i++) {
            try {
                return execute();
            } catch (QueryExecutionException | NoHostAvailableException e) {
                logger.warn("Cassandra operation failed - retrying...", e);
            } catch (Exception e) {
                throw e;
            }
            final int sleepIntervalMSec = calculateSleepInterval(i);
            logger.debug("Going to sleep {} msec before next try", sleepIntervalMSec);
            Thread.sleep(sleepIntervalMSec);
        }
    }

    protected abstract T execute() throws Exception;

    protected int calculateSleepInterval(int c) {
        return Math.min(5000, 50 * c);
    }
}
