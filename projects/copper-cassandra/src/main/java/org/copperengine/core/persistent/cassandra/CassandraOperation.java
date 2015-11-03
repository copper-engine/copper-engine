/*
 * Copyright 2002-2015 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
