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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.copperengine.management.model.AuditTrailInfo;

/**
 * Converts a list of audit trail events belonging to a single transaction into a PlantUML activity
 * diagram. The output format matches the {@code [plantuml, ..., png]} / {@code @startuml ... @enduml}
 * wrapper produced for workflow structure diagrams (see {@code PlantUmlTest} and {@code diagram.adoc}).
 *
 * <p>The caller is responsible for fetching the events upfront (e.g. via
 * {@code ScottyAuditTrailQueryEngine}); this generator performs no database access and only produces
 * markup.</p>
 */
public class AuditTrailDiagramGenerator {

    /**
     * Generates a complete PlantUML document for the given audit trail events.
     *
     * <p>Events are sorted by {@code SEQ_ID} (ascending) &mdash; exposed as {@link AuditTrailInfo#getId()}
     * &mdash; before rendering. Each distinct {@code conversationId} is rendered as a named partition; when
     * all events share a single {@code conversationId} no partition wrapper is emitted. An empty list
     * produces a minimal {@code start / :START; / :END; / stop} diagram.</p>
     *
     * @param diagramName the name embedded in the {@code [plantuml, &lt;name&gt;, png]} header
     * @param events      the audit trail events to render; not modified by this method
     * @return the complete PlantUML document as a string
     */
    public String generate(final String diagramName, final List<AuditTrailInfo> events) {
        final StringBuilder sb = new StringBuilder();
        appendHeader(sb, diagramName);

        final List<AuditTrailInfo> ordered = new ArrayList<>(events);
        ordered.sort(Comparator.comparingLong(AuditTrailInfo::getId));

        final Map<String, List<AuditTrailInfo>> byConversation = groupByConversation(ordered);

        if (byConversation.size() <= 1) {
            for (final AuditTrailInfo event : ordered) {
                appendEvent(sb, event, false);
            }
        } else {
            for (final Map.Entry<String, List<AuditTrailInfo>> entry : byConversation.entrySet()) {
                sb.append("partition ").append(entry.getKey()).append(" {\n");
                for (final AuditTrailInfo event : entry.getValue()) {
                    appendEvent(sb, event, true);
                }
                sb.append("}\n");
            }
        }

        appendFooter(sb);
        return sb.toString();
    }

    /**
     * Groups the (already SEQ_ID-ordered) events by {@code conversationId}, preserving the order in which
     * each conversation is first seen. A {@link LinkedHashMap} therefore yields partitions ordered by the
     * lowest {@code SEQ_ID} within each group.
     */
    private static Map<String, List<AuditTrailInfo>> groupByConversation(final List<AuditTrailInfo> ordered) {
        final Map<String, List<AuditTrailInfo>> byConversation = new LinkedHashMap<>();
        for (final AuditTrailInfo event : ordered) {
            byConversation.computeIfAbsent(event.getConversationId(), _ -> new ArrayList<>()).add(event);
        }
        return byConversation;
    }

    private static void appendHeader(final StringBuilder sb, final String diagramName) {
        sb.append("[plantuml, ").append(diagramName).append(", png]\n")
                .append("----\n")
                .append("\n")
                .append("@startuml\n")
                .append("start\n")
                .append(":START;\n");
    }

    private static void appendEvent(final StringBuilder sb, final AuditTrailInfo event, final boolean indented) {
        if (indented) {
            sb.append("  ");
        }
        sb.append(":").append(event.getContext()).append(" [").append(event.getOccurrence()).append("];\n");
    }

    private static void appendFooter(final StringBuilder sb) {
        sb.append(":END;\n")
                .append("stop\n")
                .append("@enduml\n")
                .append("----\n");
    }
}
