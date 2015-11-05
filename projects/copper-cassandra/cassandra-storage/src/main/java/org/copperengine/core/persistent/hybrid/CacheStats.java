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

package org.copperengine.core.persistent.hybrid;

import java.util.concurrent.atomic.AtomicLong;

public class CacheStats {

    private AtomicLong numberOfReads = new AtomicLong();
    private AtomicLong numberOfCacheHits = new AtomicLong();
    private AtomicLong numberOfCacheMisses = new AtomicLong();

    public void incNumberOfReads(boolean hit) {
        numberOfReads.incrementAndGet();
        if (hit)
            numberOfCacheHits.incrementAndGet();
        else
            numberOfCacheMisses.incrementAndGet();
    }

    public long getNumberOfCacheHits() {
        return numberOfCacheHits.get();
    }

    public long getNumberOfCacheMisses() {
        return numberOfCacheMisses.get();
    }

    public long getNumberOfReads() {
        return numberOfReads.get();
    }

    @Override
    public String toString() {
        return "CacheStats [numberOfReads=" + numberOfReads + ", numberOfCacheHits=" + numberOfCacheHits + ", numberOfCacheMisses=" + numberOfCacheMisses + "]";
    }

}
