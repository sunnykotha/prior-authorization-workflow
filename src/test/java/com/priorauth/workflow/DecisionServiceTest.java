package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DecisionServiceTest {

    private final AuditLog auditLog = new AuditLog();
    private final DecisionService service = new DecisionService(auditLog);
    private final Instant time = Instant.parse("2026-10-23T09:00:00Z");

    @Test
    void nurseCanApproveAndDecisionIsAudited() {
        AuthStatus result = service.decide(
                "PA-201", "nurse1", ReviewerRole.NURSE_REVIEWER,
                AuthStatus.UNDER_REVIEW, true, "Criteria satisfied", time);

        assertEquals(AuthStatus.APPROVED, result);
        assertEquals("APPROVED", auditLog.getEvents().get(0).eventType());
    }

    @Test
    void medicalDirectorCanDeny() {
        AuthStatus result = service.decide(
                "PA-202", "director1", ReviewerRole.MEDICAL_DIRECTOR,
                AuthStatus.UNDER_REVIEW, false, "Criteria not met", time);

        assertEquals(AuthStatus.DENIED, result);
    }

    @Test
    void nurseCannotDeny() {
        assertThrows(SecurityException.class,
                () -> service.decide(
                        "PA-203", "nurse1", ReviewerRole.NURSE_REVIEWER,
                        AuthStatus.UNDER_REVIEW, false, "Not approved", time));

        assertTrue(auditLog.getEvents().isEmpty());
    }

    @Test
    void intakeStaffCannotApprove() {
        assertThrows(SecurityException.class,
                () -> service.decide(
                        "PA-204", "intake1", ReviewerRole.INTAKE_STAFF,
                        AuthStatus.UNDER_REVIEW, true, "Approve", time));
    }

    @Test
    void decisionRequiresUnderReviewStatus() {
        assertThrows(IllegalStateException.class,
                () -> service.decide(
                        "PA-205", "nurse1", ReviewerRole.NURSE_REVIEWER,
                        AuthStatus.SUBMITTED, true, "Approve", time));
    }

    @Test
    void differentAppealReviewerCanOverturnDenial() {
        AuthStatus result = service.reviewAppeal(
                "PA-206", "reviewer2", ReviewerRole.APPEAL_REVIEWER,
                "reviewer1", AuthStatus.APPEALED, true,
                "New evidence supports approval", time);

        assertEquals(AuthStatus.OVERTURNED, result);
        assertEquals("APPEAL_OVERTURNED",
                auditLog.getEvents().get(1).eventType());
    }

    @Test
    void differentAppealReviewerCanUpholdDenial() {
        AuthStatus result = service.reviewAppeal(
                "PA-207", "reviewer2", ReviewerRole.APPEAL_REVIEWER,
                "reviewer1", AuthStatus.APPEAL_REVIEW, false,
                "Original decision upheld", time);

        assertEquals(AuthStatus.UPHELD, result);
    }

    @Test
    void originalReviewerCannotReviewOwnAppeal() {
        assertThrows(SecurityException.class,
                () -> service.reviewAppeal(
                        "PA-208", "reviewer1", ReviewerRole.APPEAL_REVIEWER,
                        "reviewer1", AuthStatus.APPEALED, true,
                        "Reconsideration", time));

        assertTrue(auditLog.getEvents().isEmpty());
    }

    @Test
    void intakeStaffCannotReviewAppeal() {
        assertThrows(SecurityException.class,
                () -> service.reviewAppeal(
                        "PA-209", "intake1", ReviewerRole.INTAKE_STAFF,
                        "reviewer1", AuthStatus.APPEALED, true,
                        "Reconsideration", time));
    }

    @Test
    void startingAppealReviewAuditsStartBeforeFinalOutcome() {
        AuthStatus result = service.reviewAppeal(
                "PA-210", "reviewer2", ReviewerRole.APPEAL_REVIEWER,
                "reviewer1", AuthStatus.APPEALED, true,
                "New evidence supports approval", time);

        var events = auditLog.getEventsForRequest("PA-210");

        assertEquals(AuthStatus.OVERTURNED, result);
        assertEquals(2, events.size());

        assertEquals("APPEAL_REVIEW_STARTED", events.get(0).eventType());
        assertEquals("reviewer2", events.get(0).actor());
        assertEquals(time, events.get(0).timestamp());
        assertFalse(events.get(0).reason().isBlank());

        assertEquals("APPEAL_OVERTURNED", events.get(1).eventType());
        assertEquals("reviewer2", events.get(1).actor());
        assertEquals(time, events.get(1).timestamp());
        assertEquals("New evidence supports approval", events.get(1).reason());
    }
}