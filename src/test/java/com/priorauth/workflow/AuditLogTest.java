package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {

    @Test
    void recordsAndRetrievesEventsInOrder() {
        AuditLog log = new AuditLog();
        Instant first = Instant.parse("2026-10-23T09:00:00Z");
        Instant second = Instant.parse("2026-10-23T10:00:00Z");

        log.record("PA-101", first, "nurse1",
                "REVIEW_STARTED", "Initial clinical review");
        log.record("PA-101", second, "nurse1",
                "INFO_REQUESTED", "Need clinical notes");

        List<AuditLog.AuditEvent> events = log.getEvents();

        assertEquals(2, events.size());
        assertEquals("REVIEW_STARTED", events.get(0).eventType());
        assertEquals("INFO_REQUESTED", events.get(1).eventType());
    }

    @Test
    void filtersEventsByRequestId() {
        AuditLog log = new AuditLog();
        Instant time = Instant.parse("2026-10-23T09:00:00Z");

        log.record("PA-101", time, "nurse1",
                "REVIEW_STARTED", "Review started");
        log.record("PA-102", time, "nurse2",
                "REVIEW_STARTED", "Review started");

        assertEquals(1, log.getEventsForRequest("PA-101").size());
        assertEquals("PA-101",
                log.getEventsForRequest("PA-101").get(0).requestId());
    }

    @Test
    void returnedEventListCannotBeModified() {
        AuditLog log = new AuditLog();
        log.record("PA-101", Instant.parse("2026-10-23T09:00:00Z"),
                "nurse1", "REVIEW_STARTED", "Review started");

        assertThrows(UnsupportedOperationException.class,
                () -> log.getEvents().clear());
    }

    @Test
    void rejectsBlankRequestId() {
        AuditLog log = new AuditLog();

        assertThrows(IllegalArgumentException.class,
                () -> log.record(" ", Instant.now(), "nurse1",
                        "REVIEW_STARTED", "Review started"));
    }

    @Test
    void rejectsMissingTimestamp() {
        AuditLog log = new AuditLog();

        assertThrows(NullPointerException.class,
                () -> log.record("PA-101", null, "nurse1",
                        "REVIEW_STARTED", "Review started"));
    }

    @Test
    void rejectsBlankActorEventTypeAndReason() {
        AuditLog log = new AuditLog();
        Instant time = Instant.parse("2026-10-23T09:00:00Z");

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> log.record("PA-101", time, " ",
                                "REVIEW_STARTED", "Review started")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> log.record("PA-101", time, "nurse1",
                                " ", "Review started")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> log.record("PA-101", time, "nurse1",
                                "REVIEW_STARTED", " "))
        );
    }
}