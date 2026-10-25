package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationDecisionWorkflowTest {

    private static final Instant DECISION_AT =
            Instant.parse("2026-10-24T10:00:00Z");
    private static final Instant EFFECTIVE_AT =
            Instant.parse("2026-10-24T00:00:00Z");
    private static final Instant EXPIRES_AT =
            Instant.parse("2027-10-24T00:00:00Z");

    @Test
    void approvedDecisionCreatesAuthorizationAndAllowsClaimConsumption() {
        AuditLog auditLog = new AuditLog();
        AuthorizationDecisionWorkflow workflow =
                new AuthorizationDecisionWorkflow(new DecisionService(auditLog));

        AuthStatus result = workflow.decide(
                "PA-WF-1", "nurse-1", ReviewerRole.NURSE_REVIEWER,
                AuthStatus.UNDER_REVIEW, true, "Routine request approved",
                DECISION_AT, EFFECTIVE_AT, EXPIRES_AT, 10);

        assertEquals(AuthStatus.APPROVED, result);
        assertTrue(workflow.findApproval("PA-WF-1").isPresent());

        workflow.consumeClaim(
                "PA-WF-1", "CLAIM-WF-1", 4, DECISION_AT);

        ApprovedAuthorization authorization =
                workflow.findApproval("PA-WF-1").orElseThrow();
        assertEquals(4, authorization.getConsumedUnits());
        assertEquals(6, authorization.getRemainingUnits());

        assertEquals("APPROVED",
                auditLog.getEventsForRequest("PA-WF-1").get(0).eventType());
    }

    @Test
    void deniedDecisionDoesNotCreateAuthorization() {
        AuditLog auditLog = new AuditLog();
        AuthorizationDecisionWorkflow workflow =
                new AuthorizationDecisionWorkflow(new DecisionService(auditLog));

        AuthStatus result = workflow.decide(
                "PA-WF-2", "director-1", ReviewerRole.MEDICAL_DIRECTOR,
                AuthStatus.UNDER_REVIEW, false, "Clinical criteria not met",
                DECISION_AT, null, null, 0);

        assertEquals(AuthStatus.DENIED, result);
        assertTrue(workflow.findApproval("PA-WF-2").isEmpty());
        assertThrows(IllegalStateException.class, () ->
                workflow.consumeClaim("PA-WF-2", "CLAIM-WF-2", 1, DECISION_AT));
    }

    @Test
    void rejectsDuplicateAuthorizationBeforeRecordingAnotherDecision() {
        AuditLog auditLog = new AuditLog();
        AuthorizationDecisionWorkflow workflow =
                new AuthorizationDecisionWorkflow(new DecisionService(auditLog));

        workflow.decide(
                "PA-WF-3", "nurse-1", ReviewerRole.NURSE_REVIEWER,
                AuthStatus.UNDER_REVIEW, true, "Approved",
                DECISION_AT, EFFECTIVE_AT, EXPIRES_AT, 5);

        assertThrows(IllegalStateException.class, () ->
                workflow.decide(
                        "PA-WF-3", "nurse-1", ReviewerRole.NURSE_REVIEWER,
                        AuthStatus.UNDER_REVIEW, true, "Duplicate attempt",
                        DECISION_AT, EFFECTIVE_AT, EXPIRES_AT, 5));

        assertEquals(1, auditLog.getEventsForRequest("PA-WF-3").size());
    }
}