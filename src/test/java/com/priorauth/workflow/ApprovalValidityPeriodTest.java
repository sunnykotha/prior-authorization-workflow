package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ApprovalValidityPeriodTest {

    private final Instant effective =
            Instant.parse("2026-10-01T00:00:00Z");
    private final Instant expiration =
            Instant.parse("2027-10-01T00:00:00Z");

    @Test
    void approvalIsValidAtItsEffectiveInstant() {
        ApprovalValidityPeriod period =
                new ApprovalValidityPeriod(effective, expiration);

        assertTrue(period.isValidAt(effective));
    }

    @Test
    void approvalIsValidImmediatelyBeforeExpiration() {
        ApprovalValidityPeriod period =
                new ApprovalValidityPeriod(effective, expiration);

        assertTrue(period.isValidAt(expiration.minusNanos(1)));
    }

    @Test
    void approvalIsInvalidBeforeEffectiveDateAndAtExpiration() {
        ApprovalValidityPeriod period =
                new ApprovalValidityPeriod(effective, expiration);

        assertFalse(period.isValidAt(effective.minusNanos(1)));
        assertFalse(period.isValidAt(expiration));
        assertFalse(period.isValidAt(expiration.plusSeconds(1)));
    }

    @Test
    void rejectsInvalidDateRanges() {
        assertThrows(IllegalArgumentException.class,
                () -> new ApprovalValidityPeriod(effective, effective));

        assertThrows(IllegalArgumentException.class,
                () -> new ApprovalValidityPeriod(
                        expiration, effective));
    }

    @Test
    void rejectsMissingDatesAndCheckTimestamp() {
        assertThrows(NullPointerException.class,
                () -> new ApprovalValidityPeriod(null, expiration));

        assertThrows(NullPointerException.class,
                () -> new ApprovalValidityPeriod(effective, null));

        ApprovalValidityPeriod period =
                new ApprovalValidityPeriod(effective, expiration);

        assertThrows(NullPointerException.class,
                () -> period.isValidAt(null));
    }

    @Test
    void requireValidAtRejectsDatesOutsideValidityWindow() {
        ApprovalValidityPeriod period =
                new ApprovalValidityPeriod(effective, expiration);

        assertDoesNotThrow(() -> period.requireValidAt(effective));
        assertDoesNotThrow(() ->
                period.requireValidAt(expiration.minusSeconds(1)));

        assertThrows(IllegalStateException.class,
                () -> period.requireValidAt(effective.minusSeconds(1)));

        assertThrows(IllegalStateException.class,
                () -> period.requireValidAt(expiration));
    }
}