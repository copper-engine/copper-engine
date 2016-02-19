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
package org.copperengine.monitoring.server.workaround;

import static org.junit.Assert.assertEquals;

import org.copperengine.core.persistent.DBProcessingState;
import org.copperengine.monitoring.core.model.WorkflowInstanceState;
import org.junit.Test;

public class DBProcessingStateWorkaroundTest {

    final DBProcessingStateWorkaround[] states = new DBProcessingStateWorkaround[DBProcessingStateWorkaround.values().length];

    @Test
    public void test_1_to_1_mapping_between_DBProcessingStateWorkaround_and_WorkflowInstanceState_() {
        for (DBProcessingStateWorkaround s : DBProcessingStateWorkaround.values()) {
            if (states[s.key()] != null)
                throw new RuntimeException("Inconsistent key mapping found for " + s);
            states[s.key()] = s;
            WorkflowInstanceState.valueOf(s.name());
        }
    }

    @Test
    public void test_1_to_1_mapping_between_WorkflowInstanceState_and_DBProcessingStateWorkaround() {
        for (WorkflowInstanceState s : WorkflowInstanceState.values()) {
            DBProcessingStateWorkaround.valueOf(s.name());
        }
    }

    @Test
    public void test_1_to_1_mapping_between_DBProcessingStateWorkaround_and_DBProcessingState() {
        for (DBProcessingStateWorkaround s : DBProcessingStateWorkaround.values()) {
            if (states[s.key()] != null)
                throw new RuntimeException("Inconsistent key mapping found for " + s);
            states[s.key()] = s;
            DBProcessingState.valueOf(s.name());
        }
    }

    @Test
    public void test_1_to_1_mapping_between_DBProcessingState_DBProcessingStateWorkaround() {
        for (DBProcessingState s : DBProcessingState.values()) {
            DBProcessingStateWorkaround.valueOf(s.name());
        }
    }

    @Test
    public void test_fromKey() {
        assertEquals(DBProcessingStateWorkaround.ENQUEUED, DBProcessingStateWorkaround.fromKey(0));
    }
}
