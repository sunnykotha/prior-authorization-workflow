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

## Worked SLA example

Consider a standard-priority MRI authorization with a 14-day SLA. The timer counts business time only, excluding weekends and configured holidays.

1. The request accumulates 3 active business days of review time.
2. The workflow requests additional information, pausing the SLA timer.
3. The requested documents arrive 4 calendar days later. The wait does not count toward active review time.
4. Review resumes with 3 active days already used.
5. Escalation is due when active review time reaches 80% of the 14-day allowance: 14 × 0.80 = 11.2 active days. After resumption, another 8.2 active days are needed to reach that threshold.

The actual escalation date depends on the business calendar, configured holidays, and review timestamps. The timer must be checked against accumulated active time, not calendar time alone.
