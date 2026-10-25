package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ApprovedAuthorizationTest {

    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant EXPIRY = Instant.parse("2027-01-01T00:00:00Z");

    @Test
    void rejectsCreationForNonApprovedDecision() {
        assertThrows(IllegalArgumentException.class, () ->
                new ApprovedAuthorization(
                        AuthStatus.DENIED, START, EXPIRY, 10));
    }

    @Test
    void acceptsClaimOnEffectiveDate() {
        ApprovedAuthorization authorization = new ApprovedAuthorization(
                AuthStatus.APPROVED, START, EXPIRY, 10);

        assertDoesNotThrow(() ->
                authorization.consumeClaim("CLAIM-1", 3, START));

        assertEquals(3, authorization.getConsumedUnits());
        assertEquals(7, authorization.getRemainingUnits());
    }

    @Test
    void rejectsClaimBeforeEffectiveDate() {
        ApprovedAuthorization authorization = new ApprovedAuthorization(
                AuthStatus.APPROVED, START, EXPIRY, 10);

        assertThrows(IllegalStateException.class, () ->
                authorization.consumeClaim(
                        "CLAIM-1", 2, START.minusSeconds(1)));

        assertEquals(0, authorization.getConsumedUnits());
    }

    @Test
    void rejectsClaimAtExclusiveExpiryBoundary() {
        ApprovedAuthorization authorization = new ApprovedAuthorization(
                AuthStatus.APPROVED, START, EXPIRY, 10);

        assertThrows(IllegalStateException.class, () ->
                authorization.consumeClaim("CLAIM-1", 2, EXPIRY));

        assertEquals(0, authorization.getConsumedUnits());
    }

    @Test
    void rejectsConsumptionBeyondApprovedUnits() {
        ApprovedAuthorization authorization = new ApprovedAuthorization(
                AuthStatus.APPROVED, START, EXPIRY, 5);

        authorization.consumeClaim("CLAIM-1", 4, START);

        assertThrows(IllegalStateException.class, () ->
                authorization.consumeClaim("CLAIM-2", 2, START));

        assertEquals(4, authorization.getConsumedUnits());
        assertEquals(1, authorization.getRemainingUnits());
    }

    @Test
    void rejectsDuplicateClaimId() {
        ApprovedAuthorization authorization = new ApprovedAuthorization(
                AuthStatus.APPROVED, START, EXPIRY, 10);

        authorization.consumeClaim("CLAIM-1", 3, START);

        assertThrows(IllegalStateException.class, () ->
                authorization.consumeClaim("CLAIM-1", 2, START));

        assertEquals(3, authorization.getConsumedUnits());
    }
}