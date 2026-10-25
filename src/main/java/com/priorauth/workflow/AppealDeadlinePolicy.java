package com.priorauth.workflow;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Defines the deadline for appealing a denial.
 * An appeal submitted exactly at the deadline is permitted.
 */
public final class AppealDeadlinePolicy {

    private final Instant denialAt;
    private final Duration appealWindow;

    public AppealDeadlinePolicy(Instant denialAt, Duration appealWindow) {
        this.denialAt = Objects.requireNonNull(
                denialAt, "Denial timestamp is required");
        this.appealWindow = Objects.requireNonNull(
                appealWindow, "Appeal window is required");

        if (appealWindow.isNegative() || appealWindow.isZero()) {
            throw new IllegalArgumentException(
                    "Appeal window must be greater than zero");
        }
    }

    public Instant getDenialAt() {
        return denialAt;
    }

    public Duration getAppealWindow() {
        return appealWindow;
    }

    public Instant getDeadline() {
        return denialAt.plus(appealWindow);
    }

    public boolean isAppealAllowedAt(Instant appealAt) {
        Objects.requireNonNull(appealAt, "Appeal timestamp is required");
        return !appealAt.isBefore(denialAt)
                && !appealAt.isAfter(getDeadline());
    }

    public void requireAppealAllowedAt(Instant appealAt) {
        if (!isAppealAllowedAt(appealAt)) {
            throw new IllegalStateException(
                    "Appeal is outside the permitted appeal window");
        }
    }
}