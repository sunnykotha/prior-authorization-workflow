package com.priorauth.workflow;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Tracks approved units and claim consumption for one authorization.
 */
public final class ApprovedUnitsLedger {

    private final int approvedUnits;
    private final Map<String, Integer> claimConsumptions =
            new LinkedHashMap<>();
    private int consumedUnits;

    public ApprovedUnitsLedger(int approvedUnits) {
        if (approvedUnits <= 0) {
            throw new IllegalArgumentException(
                    "Approved units must be greater than zero");
        }
        this.approvedUnits = approvedUnits;
    }

    /**
     * Records a claim once. A rejected claim does not change the ledger.
     */
    public synchronized void consume(
            String claimId, int units) {
        if (claimId == null || claimId.isBlank()) {
            throw new IllegalArgumentException(
                    "Claim ID must not be blank");
        }
        String normalizedClaimId = claimId.trim();

        if (units <= 0) {
            throw new IllegalArgumentException(
                    "Claim units must be greater than zero");
        }

        if (claimConsumptions.containsKey(normalizedClaimId)) {
            throw new IllegalStateException(
                    "Claim has already been recorded: " + normalizedClaimId);
        }

        if (units > getRemainingUnits()) {
            throw new IllegalStateException(
                    "Claim exceeds remaining approved units");
        }

        claimConsumptions.put(normalizedClaimId, units);
        consumedUnits += units;
    }

    public int getApprovedUnits() {
        return approvedUnits;
    }

    public synchronized int getConsumedUnits() {
        return consumedUnits;
    }

    public synchronized int getRemainingUnits() {
        return approvedUnits - consumedUnits;
    }

    public synchronized Map<String, Integer> getClaimConsumptions() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<>(claimConsumptions));
    }

    public synchronized boolean isFullyConsumed() {
        return consumedUnits == approvedUnits;
    }
}