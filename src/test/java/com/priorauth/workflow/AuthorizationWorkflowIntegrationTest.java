package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizationWorkflowIntegrationTest {

    @Test
    void shouldAutoApproveEligibleRequestAndRecordAuditEvent() {
        Instant submittedAt = Instant.parse("2026-10-20T09:00:00Z");
        Instant decisionAt = Instant.parse("2026-10-20T09:15:00Z");

        AuthorizationRequest request = new AuthorizationRequest(
                "PA-1001", "MRI-LOW-RISK", RequestPriority.STANDARD,
                submittedAt, Set.of("MRI_REPORT"));

        AuditLog auditLog = new AuditLog();
        AutoApprovalService autoApprovalService = new AutoApprovalService(
                Set.of("MRI-LOW-RISK"), auditLog);
        StateTransitionService transitionService =
                new StateTransitionService();

        assertTrue(transitionService.canTransition(
                AuthStatus.SUBMITTED, AuthStatus.UNDER_REVIEW));
        assertFalse(transitionService.canTransition(
                AuthStatus.SUBMITTED, AuthStatus.APPROVED));

        AuthStatus status = autoApprovalService.evaluate(
                request, AuthStatus.SUBMITTED, decisionAt);

        assertEquals(AuthStatus.SUBMITTED, status);
        assertTrue(auditLog.getEventsForRequest("PA-1001").isEmpty());

        request.receiveDocument("MRI_REPORT");
        assertTrue(request.isDocumentationComplete());

        status = autoApprovalService.evaluate(
                request, AuthStatus.SUBMITTED, decisionAt);

        assertEquals(AuthStatus.APPROVED, status);

        var events = auditLog.getEventsForRequest("PA-1001");
        assertEquals(1, events.size());
        assertEquals("AUTO_APPROVAL", events.get(0).actor());
        assertEquals("AUTO_APPROVED", events.get(0).eventType());
        assertEquals(decisionAt, events.get(0).timestamp());
    }
}