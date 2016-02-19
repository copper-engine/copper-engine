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

import java.util.HashMap;
import java.util.Map;

/**
 * Internally used class.
 *
 * @author austermann
 */
final class TimeoutSlot {

    private final long timeoutTS;
    private final Map<String, Runnable> wfId2RunnableMap = new HashMap<>();

    public TimeoutSlot(long timeoutTS) {
        assert timeoutTS > 0;
        this.timeoutTS = timeoutTS;
    }

    public long getTimeoutTS() {
        return timeoutTS;
    }

    public Map<String, Runnable> getWfId2RunnableMap() {
        return wfId2RunnableMap;
    }
}