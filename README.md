# Prior Authorization Workflow

A Java 21 and Maven project implementing a prior authorization lifecycle.

## Requirements
- JDK 21
- Maven Wrapper included in the repository

## Run tests on Windows
```powershell
.\mvnw.cmd test
```

## Features
- Authorization lifecycle and state transitions
- Role-based permissions and independent appeal review
- Automatic approval with complete documentation
- Standard and urgent SLA timers
- Pausing and resuming SLA timers for information requests
- Weekend and configured-holiday handling
- Escalation at 80% and SLA breach reporting
- Information-request closure and amendments
- Duplicate request detection and appeal deadlines
- Audit event recording
- Approval validity and approved-unit consumption

## Project structure
- `src/main/java/com/priorauth/workflow/`: application code
- `src/test/java/com/priorauth/workflow/`: automated tests
- `pom.xml`: Maven configuration

## Test baseline
The recorded baseline is 151 passing tests. Run the command above to verify.
