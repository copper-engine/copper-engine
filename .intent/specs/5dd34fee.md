# AuditTrailDiagramGeneratorTest — Resource-Based Expected Diagram

## Intent

Replace any inline expected-diagram strings in `AuditTrailDiagramGeneratorTest` with dedicated `.adoc` resource files, following the same pattern already established by `PlantUmlTest` and `diagram.adoc`. This makes expected diagrams human-readable, diff-friendly, and easy to update without touching test source code.

## Summary

> "For the result diagram in AuditTrailDiagramGeneratorTest, use a resource that is structured like diagram.adoc!"

Each test case that asserts a full diagram output loads its expected content from a named `.adoc` file stored in the test resources directory, rather than comparing against a string literal embedded in the test code. The file format and the loading mechanism mirror `diagram.adoc` exactly.

## How It Works

**Resource file format**

Each file is a plain `.adoc` file containing the complete expected PlantUML document, wrapped in the AsciiDoc block header — identical in structure to `diagram.adoc`:

```
[plantuml, <diagram-name>, png]
----

@startuml
start
:START;
...
:END;
stop
@enduml
----
```

**One file per test case**

Each scenario that verifies a full diagram output has its own named resource file placed in the test resources directory alongside the existing `diagram.adoc`:

| Test case | Resource file |
|---|---|
| Empty event list | `audit-trail-diagram-empty.adoc` |
| Single conversation | `audit-trail-diagram-single.adoc` |
| Multiple conversations | `audit-trail-diagram-multi.adoc` |
| Unordered input (SEQ_ID sort) | `audit-trail-diagram-ordered.adoc` |

**Loading mechanism**

Each test case loads its expected diagram the same way `PlantUmlTest` does:

1. Open the resource file via `getResourceAsStream`
2. Read all bytes and decode as UTF-8
3. Normalise line endings (`\r\n` → `\n`)
4. Assert equality against the actual generated output

## Included / Not Included

**Included**
- One `.adoc` resource file per test case that asserts a full diagram
- Loading, UTF-8 decoding, and line-ending normalisation in the test, matching the `PlantUmlTest` pattern
- Removal of any inline expected-diagram strings from the test code

**Not included**
- Changes to the diagram generator implementation itself
- A shared base class or utility for resource loading
- Resource files for test cases that only check structural properties (null checks, exception types, etc.) rather than full diagram content
