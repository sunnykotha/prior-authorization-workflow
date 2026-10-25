package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AppealDeadlinePolicyTest {

    private final Instant denialAt =
            Instant.parse("2026-10-01T12:00:00Z");
    private final Duration appealWindow = Duration.ofDays(30);

    @Test
    void calculatesAppealDeadline() {
        AppealDeadlinePolicy policy =
                new AppealDeadlinePolicy(denialAt, appealWindow);

        assertEquals(denialAt.plus(Duration.ofDays(30)), policy.getDeadline());
        assertEquals(denialAt, policy.getDenialAt());
        assertEquals(appealWindow, policy.getAppealWindow());
    }

    @Test
    void permitsAppealBeforeDeadline() {
        AppealDeadlinePolicy policy =
                new AppealDeadlinePolicy(denialAt, appealWindow);

        assertTrue(policy.isAppealAllowedAt(denialAt));
        assertTrue(policy.isAppealAllowedAt(denialAt.plus(Duration.ofDays(10))));
        assertTrue(policy.isAppealAllowedAt(
                policy.getDeadline().minusNanos(1)));
    }

    @Test
    void permitsAppealExactlyAtDeadline() {
        AppealDeadlinePolicy policy =
                new AppealDeadlinePolicy(denialAt, appealWindow);

        assertTrue(policy.isAppealAllowedAt(policy.getDeadline()));
        assertDoesNotThrow(() ->
                policy.requireAppealAllowedAt(policy.getDeadline()));
    }

    @Test
    void rejectsAppealAfterDeadlineOrBeforeDenial() {
        AppealDeadlinePolicy policy =
                new AppealDeadlinePolicy(denialAt, appealWindow);

        assertFalse(policy.isAppealAllowedAt(
                policy.getDeadline().plusNanos(1)));
        assertFalse(policy.isAppealAllowedAt(
                denialAt.minusNanos(1)));

        assertThrows(IllegalStateException.class, () ->
                policy.requireAppealAllowedAt(
                        policy.getDeadline().plusSeconds(1)));
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(NullPointerException.class, () ->
                new AppealDeadlinePolicy(null, appealWindow));
        assertThrows(NullPointerException.class, () ->
                new AppealDeadlinePolicy(denialAt, null));
        assertThrows(IllegalArgumentException.class, () ->
                new AppealDeadlinePolicy(denialAt, Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () ->
                new AppealDeadlinePolicy(denialAt, Duration.ofSeconds(-1)));
    }

    @Test
    void rejectsNullAppealTimestamp() {
        AppealDeadlinePolicy policy =
                new AppealDeadlinePolicy(denialAt, appealWindow);

        assertThrows(NullPointerException.class, () ->
                policy.isAppealAllowedAt(null));
    }
}