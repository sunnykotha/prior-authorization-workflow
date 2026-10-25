package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApprovedUnitsLedgerTest {

    @Test
    void tracksPartialConsumptionAndRemainingUnits() {
        ApprovedUnitsLedger ledger = new ApprovedUnitsLedger(10);

        ledger.consume("CLAIM-1", 3);
        ledger.consume("CLAIM-2", 2);

        assertEquals(10, ledger.getApprovedUnits());
        assertEquals(5, ledger.getConsumedUnits());
        assertEquals(5, ledger.getRemainingUnits());
        assertFalse(ledger.isFullyConsumed());
    }

    @Test
    void allowsConsumptionUpToExactApprovedLimit() {
        ApprovedUnitsLedger ledger = new ApprovedUnitsLedger(10);

        ledger.consume("CLAIM-1", 4);
        ledger.consume("CLAIM-2", 6);

        assertEquals(10, ledger.getConsumedUnits());
        assertEquals(0, ledger.getRemainingUnits());
        assertTrue(ledger.isFullyConsumed());
    }

    @Test
    void rejectsClaimThatExceedsRemainingUnitsWithoutChangingLedger() {
        ApprovedUnitsLedger ledger = new ApprovedUnitsLedger(10);
        ledger.consume("CLAIM-1", 7);

        assertThrows(IllegalStateException.class,
                () -> ledger.consume("CLAIM-2", 4));

        assertEquals(7, ledger.getConsumedUnits());
        assertEquals(3, ledger.getRemainingUnits());
        assertFalse(ledger.getClaimConsumptions().containsKey("CLAIM-2"));
    }

    @Test
    void rejectsDuplicateClaimIdWithoutDoubleCounting() {
        ApprovedUnitsLedger ledger = new ApprovedUnitsLedger(10);
        ledger.consume("CLAIM-1", 4);

        assertThrows(IllegalStateException.class,
                () -> ledger.consume("CLAIM-1", 4));

        assertEquals(4, ledger.getConsumedUnits());
        assertEquals(6, ledger.getRemainingUnits());
    }

    @Test
    void rejectsInvalidApprovedAndClaimUnits() {
        assertThrows(IllegalArgumentException.class,
                () -> new ApprovedUnitsLedger(0));
        assertThrows(IllegalArgumentException.class,
                () -> new ApprovedUnitsLedger(-1));

        ApprovedUnitsLedger ledger = new ApprovedUnitsLedger(10);

        assertThrows(IllegalArgumentException.class,
                () -> ledger.consume("CLAIM-1", 0));
        assertThrows(IllegalArgumentException.class,
                () -> ledger.consume("CLAIM-1", -1));
        assertThrows(IllegalArgumentException.class,
                () -> ledger.consume(" ", 1));

        assertEquals(0, ledger.getConsumedUnits());
    }

    @Test
    void exposesAnImmutableSnapshotOfClaimConsumption() {
        ApprovedUnitsLedger ledger = new ApprovedUnitsLedger(10);
        ledger.consume("CLAIM-1", 3);

        Map<String, Integer> snapshot = ledger.getClaimConsumptions();

        assertEquals(Map.of("CLAIM-1", 3), snapshot);
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.put("CLAIM-2", 2));

        ledger.consume("CLAIM-2", 2);
        assertEquals(1, snapshot.size());
        assertEquals(2, ledger.getClaimConsumptions().size());
    }
}