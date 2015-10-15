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
package org.copperengine.core.monitoring;

import java.util.concurrent.TimeUnit;

/**
 * Helper class for collecting SQL statement related runtime statistics.
 * Offers a start and stop method, that encapsulate the measurement of the elapsed time.
 *
 * @author austermann
 */
public class StmtStatistic {

    private ThreadLocal<long[]> startTS = new ThreadLocal<long[]>() {
        protected long[] initialValue() {
            return new long[1];
        }
    };

    private final RuntimeStatisticsCollector runtimeStatisticsCollector;
    private final String measurePointId;

    /**
     * creates a new StmtStatistic with a name only.
     *
     * @param measurePointId
     * @param runtimeStatisticsCollector
     */
    public StmtStatistic(final String measurePointId, final RuntimeStatisticsCollector runtimeStatisticsCollector) {
        if (measurePointId == null)
            throw new NullPointerException();
        if (runtimeStatisticsCollector == null)
            throw new NullPointerException();
        this.measurePointId = measurePointId;
        this.runtimeStatisticsCollector = runtimeStatisticsCollector;
    }

    public void start() {
        ((long[]) startTS.get())[0] = System.nanoTime();
    }

    public long stop(int updateCount) {
        long et = System.nanoTime() - startTS.get()[0];
        runtimeStatisticsCollector.submit(measurePointId, updateCount, et, TimeUnit.NANOSECONDS);
        return et;
    }
}
