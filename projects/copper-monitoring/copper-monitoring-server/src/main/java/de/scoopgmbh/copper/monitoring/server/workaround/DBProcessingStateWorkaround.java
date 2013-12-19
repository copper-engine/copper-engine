/*
 * Copyright 2002-2013 SCOOP Software GmbH
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
package de.scoopgmbh.copper.monitoring.server.workaround;

import java.util.HashMap;
import java.util.Map;

import de.scoopgmbh.copper.monitoring.core.model.WorkflowInstanceState;

public enum DBProcessingStateWorkaround {
    ENQUEUED(0),
    PROCESSING(1) /* so far unused */,
    WAITING(2),
    FINISHED(3),
    INVALID(4),
    ERROR(5);

    static final Map<Integer, DBProcessingStateWorkaround> states = new HashMap<Integer, DBProcessingStateWorkaround>();
    static {
        for (DBProcessingStateWorkaround state : DBProcessingStateWorkaround.values()) {
            states.put(state.key(), state);
        }
    }

    final int key;

    DBProcessingStateWorkaround(int key) {
        this.key = key;
    }

    public int key() {
        return key;
    }

    public WorkflowInstanceState asWorkflowInstanceState() {
        return WorkflowInstanceState.valueOf(name());
    }

    public static DBProcessingStateWorkaround fromKey(int key) {
        DBProcessingStateWorkaround state = states.get(key);
        if (state == null)
            throw new IllegalArgumentException("No value for " + key);
        return state;
    }

    public static DBProcessingStateWorkaround fromWorkflowInstanceState(WorkflowInstanceState workflowInstanceState) {
        return DBProcessingStateWorkaround.valueOf(workflowInstanceState.name());
    }

}
