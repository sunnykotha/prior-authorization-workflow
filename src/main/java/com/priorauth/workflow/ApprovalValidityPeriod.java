package com.priorauth.workflow;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents the effective validity window of an authorization approval.
 * The start is inclusive and the expiration instant is exclusive.
 */
public final class ApprovalValidityPeriod {

    private final Instant effectiveAt;
    private final Instant expiresAt;

    public ApprovalValidityPeriod(Instant effectiveAt, Instant expiresAt) {
        this.effectiveAt = Objects.requireNonNull(
                effectiveAt, "Effective date is required");
        this.expiresAt = Objects.requireNonNull(
                expiresAt, "Expiration date is required");

        if (!expiresAt.isAfter(effectiveAt)) {
            throw new IllegalArgumentException(
                    "Expiration date must be after the effective date");
        }
    }

    public Instant getEffectiveAt() {
        return effectiveAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isValidAt(Instant timestamp) {
        Objects.requireNonNull(timestamp, "Timestamp is required");
        return !timestamp.isBefore(effectiveAt)
                && timestamp.isBefore(expiresAt);
    }

    public void requireValidAt(Instant timestamp) {
        if (!isValidAt(timestamp)) {
            throw new IllegalStateException(
                    "Authorization approval is not valid at the requested time");
        }
    }
}