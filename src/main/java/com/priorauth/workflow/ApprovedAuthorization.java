package com.priorauth.workflow;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Combines an approved decision, its validity window, and its unit allowance.
 * Claim consumption is permitted only while the approval is valid.
 */
public final class ApprovedAuthorization {
    private final ApprovalValidityPeriod validityPeriod;
    private final ApprovedUnitsLedger unitsLedger;

    public ApprovedAuthorization(
            AuthStatus decisionStatus,
            Instant effectiveAt,
            Instant expiresAt,
            int approvedUnits) {

        if (decisionStatus != AuthStatus.APPROVED) {
            throw new IllegalArgumentException(
                    "Only approved requests can create an approved authorization.");
        }

        this.validityPeriod = new ApprovalValidityPeriod(
                Objects.requireNonNull(effectiveAt, "effectiveAt"),
                Objects.requireNonNull(expiresAt, "expiresAt"));
        this.unitsLedger = new ApprovedUnitsLedger(approvedUnits);
    }

    public synchronized void consumeClaim(
            String claimId, int units, Instant serviceAt) {

        Objects.requireNonNull(serviceAt, "serviceAt");

        if (!validityPeriod.isValidAt(serviceAt)) {
            throw new IllegalStateException(
                    "The authorization is not valid on the service date.");
        }

        unitsLedger.consume(claimId, units);
    }

    public Instant getEffectiveAt() {
        return validityPeriod.getEffectiveAt();
    }

    public Instant getExpiresAt() {
        return validityPeriod.getExpiresAt();
    }

    public int getApprovedUnits() {
        return unitsLedger.getApprovedUnits();
    }

    public int getConsumedUnits() {
        return unitsLedger.getConsumedUnits();
    }

    public int getRemainingUnits() {
        return unitsLedger.getRemainingUnits();
    }

    public synchronized Map<String, Integer> getClaimConsumptions() {
        return unitsLedger.getClaimConsumptions();
    }

    public boolean isFullyConsumed() {
        return unitsLedger.isFullyConsumed();
    }
}