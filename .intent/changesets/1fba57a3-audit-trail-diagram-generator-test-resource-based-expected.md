# AuditTrailDiagramGeneratorTest resource-based expected diagram

## Summary

> "For the result diagram in AuditTrailDiagramGeneratorTest, use a resource that is structured like diagram.adoc!"

Replace inline expected-diagram strings in `AuditTrailDiagramGeneratorTest` with dedicated `.adoc` resource files, following the same pattern used by `PlantUmlTest` with `diagram.adoc`. Each test case that asserts a full diagram output loads its expected content from a named resource file instead of a hardcoded string literal.

## Acceptance Criteria

- A dedicated `.adoc` resource file exists for each test case that verifies full diagram output (empty list, single conversation, multiple conversations, SEQ_ID ordering)
- Each resource file follows the same `[plantuml, …, png] / @startuml … @enduml` structure as `diagram.adoc`
- Each test case loads its expected diagram via `getResourceAsStream`, UTF-8 decoding, and `\r\n` → `\n` normalisation — matching the `PlantUmlTest` pattern exactly
- No inline expected-diagram strings remain in the test code for full-diagram assertions
- Resource files are placed in the test resources directory alongside the existing `diagram.adoc`

## Testing Notes

- Verify each test still passes after replacing inline strings with resource-file loading
- Confirm that altering a resource file causes the corresponding test to fail (i.e. the comparison is actually exercised)


## Specs

- [AuditTrailDiagramGeneratorTest — Resource-Based Expected Diagram](../specs/5dd34fee.md)
- [audit-trail-diagram-ordered.adoc — Expected Diagram Resource for SEQ_ID Ordering Test](../specs/63b3649e.md)
- [audit-trail-diagram-ordered.adoc — Expected Diagram Resource for SEQ_ID Ordering Test](../specs/1f2bff60.md)

---

[View in Intent](https://app.onintent.build/?project=78178bb0-7757-4796-97d1-c1c67ba2d024&changeset=1fba57a3-7a7c-4f92-b019-777b794e200e&tab=detail)