package com.priorauth.workflow;

import java.time.Duration;
import java.util.Objects;

public class SlaMonitor {

    public enum Status {
        NORMAL,
        ESCALATION_REQUIRED,
        BREACHED
    }

    public record Assessment(Status status, double elapsedPercent) {}

    public Assessment assess(
            Duration elapsedReviewTime, Duration allowedSlaTime) {

        Objects.requireNonNull(
                elapsedReviewTime, "Elapsed review time is required");
        Objects.requireNonNull(
                allowedSlaTime, "Allowed SLA time is required");

        if (elapsedReviewTime.isNegative()) {
            throw new IllegalArgumentException(
                    "Elapsed review time cannot be negative");
        }

        if (allowedSlaTime.isZero() || allowedSlaTime.isNegative()) {
            throw new IllegalArgumentException(
                    "Allowed SLA time must be positive");
        }

        double elapsedPercent =
                ((double) elapsedReviewTime.toNanos()
                        / allowedSlaTime.toNanos()) * 100.0;

        Status status;
        if (elapsedPercent >= 100.0) {
            status = Status.BREACHED;
        } else if (elapsedPercent >= 80.0) {
            status = Status.ESCALATION_REQUIRED;
        } else {
            status = Status.NORMAL;
        }

        return new Assessment(status, elapsedPercent);
    }
}