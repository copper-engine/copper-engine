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
package org.copperengine.core.wfrepo.checkpoint;

import java.util.Collections;
import java.util.List;

import org.copperengine.management.model.AuditTrailInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AuditTrailDiagramGeneratorTest {

    private final AuditTrailDiagramGenerator generator = new AuditTrailDiagramGenerator();

    private static AuditTrailInfo event(final long seqId, final String conversationId, final String context, final long occurrence) {
        return new AuditTrailInfo(
                seqId,
                "tx-1",
                conversationId,
                "corr-1",
                occurrence,
                1,
                context,
                "wfi-1",
                "messageType");
    }

    @Test
    void emptyList() {
        final String diagram = generator.generate("empty", Collections.emptyList());

        Assertions.assertEquals("""
                [plantuml, empty, png]
                ----

                @startuml
                start
                :START;
                :END;
                stop
                @enduml
                ----
                """, diagram);
    }

    @Test
    void singleConversationHasNoPartition() {
        final List<AuditTrailInfo> events = List.of(
                event(1, "conv-1", "stepA", 1000),
                event(2, "conv-1", "stepB", 2000));

        final String diagram = generator.generate("single", events);

        Assertions.assertEquals("""
                [plantuml, single, png]
                ----

                @startuml
                start
                :START;
                :stepA [1000];
                :stepB [2000];
                :END;
                stop
                @enduml
                ----
                """, diagram);
    }

    @Test
    void multipleConversationsProducePartitions() {
        final List<AuditTrailInfo> events = List.of(
                event(1, "conv-1", "stepA", 1000),
                event(2, "conv-2", "stepB", 2000),
                event(3, "conv-1", "stepC", 3000));

        final String diagram = generator.generate("multi", events);

        Assertions.assertEquals("""
                [plantuml, multi, png]
                ----

                @startuml
                start
                :START;
                partition conv-1 {
                  :stepA [1000];
                  :stepC [3000];
                }
                partition conv-2 {
                  :stepB [2000];
                }
                :END;
                stop
                @enduml
                ----
                """, diagram);
    }

    @Test
    void eventsAreOrderedBySeqId() {
        // Provided out of order; SEQ_ID (id) ordering must be applied before rendering.
        final List<AuditTrailInfo> events = List.of(
                event(3, "conv-1", "third", 3000),
                event(1, "conv-1", "first", 1000),
                event(2, "conv-1", "second", 2000));

        final String diagram = generator.generate("ordering", events);

        Assertions.assertEquals("""
                [plantuml, ordering, png]
                ----

                @startuml
                start
                :START;
                :first [1000];
                :second [2000];
                :third [3000];
                :END;
                stop
                @enduml
                ----
                """, diagram);
    }
}
