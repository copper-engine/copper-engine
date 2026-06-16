# Audit Trail Transaction PlantUML Diagram Generator

## Summary

Adds an `AuditTrailDiagramGenerator` that converts a list of audit trail events for a transaction into a PlantUML activity diagram. The output format matches the existing workflow structure diagram format established by `PlantUmlTest`. Callers fetch events via the existing `ScottyAuditTrailQueryEngine` and pass them to the generator; the generator handles ordering, grouping by conversation, and markup production.

## Acceptance Criteria

- A `generate(String diagramName, List<AuditTrailInfo> events)` method exists and returns a complete, valid PlantUML document string
- Events are ordered by `SEQ_ID` ascending before rendering
- Each distinct `conversationId` is rendered as a named partition; events within a partition are labelled with their `context` and occurrence timestamp
- When all events share one `conversationId`, no partition wrapper is emitted
- An empty event list produces a valid minimal diagram (start → :START; → :END; → stop)
- The output format matches the `[plantuml, …, png] / @startuml … @enduml` wrapper used in `diagram.adoc`
- Unit tests cover: empty list, single conversation, multiple conversations, correct SEQ_ID ordering

## Testing Notes

- Unit tests should not require a running engine or database — input is a plain `List<AuditTrailInfo>` constructed inline
- Compare generated output string against expected PlantUML literals, mirroring the assertion style in `PlantUmlTest`
- Test that SEQ_ID ordering is applied even when the input list is provided in a different order


## Specs

- [Audit Trail Transaction PlantUML Diagram Generator](../specs/84094c9d.md)

---

[View in Intent](https://app.onintent.build/?project=78178bb0-7757-4796-97d1-c1c67ba2d024&changeset=be881a8d-e7d3-4ff5-8c2a-866f96dd8c88&tab=detail)