package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class RequestAmendmentTrackerTest {

    private final Instant submittedAt =
            Instant.parse("2026-10-26T09:00:00Z");

    @Test
    void startsReviewAtSubmissionWithNoAmendments() {
        RequestAmendmentTracker tracker = new RequestAmendmentTracker(
                "PA-AMEND-1", submittedAt, new AuditLog());

        assertEquals(submittedAt, tracker.getSubmittedAt());
        assertEquals(submittedAt, tracker.getReviewStartedAt());
        assertEquals(0, tracker.getRevision());
        assertTrue(tracker.getAmendments().isEmpty());
    }

    @Test
    void amendmentRecordsDetailsAndResetsReviewStart() {
        AuditLog log = new AuditLog();
        RequestAmendmentTracker tracker = new RequestAmendmentTracker(
                "PA-AMEND-1", submittedAt, log);
        Instant amendedAt = submittedAt.plusSeconds(3600);

        var amendment = tracker.amend(
                "intake-1", "Corrected procedure details", amendedAt);

        assertEquals(1, amendment.revision());
        assertEquals(amendedAt, amendment.timestamp());
        assertEquals("intake-1", amendment.actor());
        assertEquals("Corrected procedure details", amendment.reason());
        assertEquals(amendedAt, tracker.getReviewStartedAt());
        assertEquals(1, tracker.getRevision());

        var events = log.getEventsForRequest("PA-AMEND-1");
        assertEquals(1, events.size());
        assertEquals("REQUEST_AMENDED", events.get(0).eventType());
    }

    @Test
    void multipleAmendmentsKeepOrderedHistoryAndLatestReviewStart() {
        RequestAmendmentTracker tracker = new RequestAmendmentTracker(
                "PA-AMEND-1", submittedAt, new AuditLog());
        Instant first = submittedAt.plusSeconds(3600);
        Instant second = first.plusSeconds(7200);

        tracker.amend("intake-1", "Corrected code", first);
        tracker.amend("nurse-1", "Updated clinical information", second);

        assertEquals(2, tracker.getRevision());
        assertEquals(second, tracker.getReviewStartedAt());
        assertEquals(1, tracker.getAmendments().get(0).revision());
        assertEquals(2, tracker.getAmendments().get(1).revision());
    }

    @Test
    void rejectsAmendmentBeforeSubmissionOrEarlierThanReviewStart() {
        RequestAmendmentTracker tracker = new RequestAmendmentTracker(
                "PA-AMEND-1", submittedAt, new AuditLog());

        assertThrows(IllegalArgumentException.class, () ->
                tracker.amend("intake-1", "Backdated change",
                        submittedAt.minusSeconds(1)));

        Instant first = submittedAt.plusSeconds(3600);
        tracker.amend("intake-1", "First change", first);

        assertThrows(IllegalArgumentException.class, () ->
                tracker.amend("intake-1", "Out of order change",
                        first.minusSeconds(1)));

        assertEquals(1, tracker.getRevision());
        assertEquals(first, tracker.getReviewStartedAt());
    }

    @Test
    void rejectsBlankActorAndReasonWithoutChangingHistory() {
        RequestAmendmentTracker tracker = new RequestAmendmentTracker(
                "PA-AMEND-1", submittedAt, new AuditLog());
        Instant amendedAt = submittedAt.plusSeconds(60);

        assertThrows(IllegalArgumentException.class, () ->
                tracker.amend(" ", "Valid reason", amendedAt));

        assertThrows(IllegalArgumentException.class, () ->
                tracker.amend("intake-1", " ", amendedAt));

        assertEquals(0, tracker.getRevision());
        assertEquals(submittedAt, tracker.getReviewStartedAt());
    }

    @Test
    void amendmentHistoryCannotBeModifiedByCaller() {
        RequestAmendmentTracker tracker = new RequestAmendmentTracker(
                "PA-AMEND-1", submittedAt, new AuditLog());
        tracker.amend("intake-1", "Corrected details",
                submittedAt.plusSeconds(60));

        assertThrows(UnsupportedOperationException.class, () ->
                tracker.getAmendments().clear());

        assertEquals(1, tracker.getRevision());
    }
}