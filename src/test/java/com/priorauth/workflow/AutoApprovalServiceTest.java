package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AutoApprovalServiceTest {
    private final Instant time =
            Instant.parse("2026-10-23T09:00:00Z");

    @Test
    void approvesLowRiskRequestWhenDocumentsAreComplete() {
        AuditLog log = new AuditLog();
        AutoApprovalService service = new AutoApprovalService(
                Set.of("LOW-RISK-01"), log);
        AuthorizationRequest request = new AuthorizationRequest(
                "PA-301", "LOW-RISK-01", RequestPriority.STANDARD,
                time, Set.of("Clinical notes"));

        request.receiveDocument("Clinical notes");

        assertEquals(AuthStatus.APPROVED,
                service.evaluate(request, AuthStatus.SUBMITTED, time));
        assertEquals("AUTO_APPROVAL", log.getEvents().get(0).actor());
        assertEquals("AUTO_APPROVED",
                log.getEvents().get(0).eventType());
    }

    @Test
    void doesNotApproveWhenRequiredDocumentsAreMissing() {
        AutoApprovalService service = new AutoApprovalService(
                Set.of("LOW-RISK-01"), new AuditLog());
        AuthorizationRequest request = new AuthorizationRequest(
                "PA-302", "LOW-RISK-01", RequestPriority.STANDARD,
                time, Set.of("Clinical notes"));

        assertEquals(AuthStatus.SUBMITTED,
                service.evaluate(request, AuthStatus.SUBMITTED, time));
    }

    @Test
    void doesNotApproveProcedureOutsideLowRiskList() {
        AutoApprovalService service = new AutoApprovalService(
                Set.of("LOW-RISK-01"), new AuditLog());
        AuthorizationRequest request = new AuthorizationRequest(
                "PA-303", "HIGH-RISK-01", RequestPriority.STANDARD,
                time, Set.of());

        assertEquals(AuthStatus.SUBMITTED,
                service.evaluate(request, AuthStatus.SUBMITTED, time));
    }

    @Test
    void doesNotChangeRequestsAlreadyUnderReview() {
        AutoApprovalService service = new AutoApprovalService(
                Set.of("LOW-RISK-01"), new AuditLog());
        AuthorizationRequest request = new AuthorizationRequest(
                "PA-304", "LOW-RISK-01", RequestPriority.STANDARD,
                time, Set.of());

        assertEquals(AuthStatus.UNDER_REVIEW,
                service.evaluate(request, AuthStatus.UNDER_REVIEW, time));
    }

    @Test
    void doesNotCreateAuditEventWhenRequestIsNotEligible() {
        AuditLog log = new AuditLog();
        AutoApprovalService service = new AutoApprovalService(
                Set.of("LOW-RISK-01"), log);
        AuthorizationRequest request = new AuthorizationRequest(
                "PA-305", "LOW-RISK-01", RequestPriority.STANDARD,
                time, Set.of("Clinical notes"));

        service.evaluate(request, AuthStatus.SUBMITTED, time);

        assertTrue(log.getEvents().isEmpty());
    }

    @Test
    void rejectsMissingRequest() {
        AutoApprovalService service = new AutoApprovalService(
                Set.of("LOW-RISK-01"), new AuditLog());

        assertThrows(NullPointerException.class,
                () -> service.evaluate(
                        null, AuthStatus.SUBMITTED, time));
    }
}