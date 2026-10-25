package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class InfoRequestClosureServiceTest {

    private final AuditLog auditLog = new AuditLog();
    private final InfoRequestClosureService service =
            new InfoRequestClosureService(auditLog);
    private final Instant requestedAt =
            Instant.parse("2026-10-01T10:00:00Z");

    @Test
    void closesAtExactDeadlineAndRecordsAuditEvent() {
        Instant deadline = requestedAt.plusSeconds(5L * 86400);

        AuthStatus result = service.evaluate(
                "PA-401", "system", AuthStatus.INFO_REQUESTED,
                requestedAt, 5, deadline);

        assertEquals(AuthStatus.CLOSED_INSUFFICIENT_INFO, result);
        assertEquals(1, auditLog.getEvents().size());
        assertEquals("CLOSED_INSUFFICIENT_INFO",
                auditLog.getEvents().get(0).eventType());
        assertEquals("system", auditLog.getEvents().get(0).actor());
        assertEquals(deadline, auditLog.getEvents().get(0).timestamp());
    }

    @Test
    void doesNotCloseOneSecondBeforeDeadline() {
        Instant justBeforeDeadline =
                requestedAt.plusSeconds(5L * 86400 - 1);

        AuthStatus result = service.evaluate(
                "PA-406", "system", AuthStatus.INFO_REQUESTED,
                requestedAt, 5, justBeforeDeadline);

        assertEquals(AuthStatus.INFO_REQUESTED, result);
        assertTrue(auditLog.getEvents().isEmpty());
    }

    @Test
    void closesWhenDeadlineHasPassed() {
        AuthStatus result = service.evaluate(
                "PA-407", "system", AuthStatus.INFO_REQUESTED,
                requestedAt, 5, requestedAt.plusSeconds(5L * 86400 + 1));

        assertEquals(AuthStatus.CLOSED_INSUFFICIENT_INFO, result);
        assertEquals("CLOSED_INSUFFICIENT_INFO",
                auditLog.getEvents().get(0).eventType());
    }

    @Test
    void doesNotCloseBeforeDeadline() {
        AuthStatus result = service.evaluate(
                "PA-402", "system", AuthStatus.INFO_REQUESTED,
                requestedAt, 5, requestedAt.plusSeconds(4L * 86400));

        assertEquals(AuthStatus.INFO_REQUESTED, result);
        assertTrue(auditLog.getEvents().isEmpty());
    }

    @Test
    void doesNotCloseIfStatusHasChanged() {
        AuthStatus result = service.evaluate(
                "PA-403", "system", AuthStatus.UNDER_REVIEW,
                requestedAt, 5, requestedAt.plusSeconds(10L * 86400));

        assertEquals(AuthStatus.UNDER_REVIEW, result);
        assertTrue(auditLog.getEvents().isEmpty());
    }

    @Test
    void rejectsNonPositiveAllowedDays() {
        assertThrows(IllegalArgumentException.class,
                () -> service.evaluate(
                        "PA-404", "system", AuthStatus.INFO_REQUESTED,
                        requestedAt, 0, requestedAt));
    }

    @Test
    void rejectsCurrentTimeBeforeRequestTime() {
        assertThrows(IllegalArgumentException.class,
                () -> service.evaluate(
                        "PA-405", "system", AuthStatus.INFO_REQUESTED,
                        requestedAt, 5, requestedAt.minusSeconds(1)));
    }

    @Test
    void closureStatusHasNoOutgoingTransitions() {
        StateTransitionService transitions = new StateTransitionService();

        assertFalse(transitions.canTransition(
                AuthStatus.CLOSED_INSUFFICIENT_INFO,
                AuthStatus.UNDER_REVIEW));
        assertFalse(transitions.canTransition(
                AuthStatus.CLOSED_INSUFFICIENT_INFO,
                AuthStatus.APPROVED));
    }
}