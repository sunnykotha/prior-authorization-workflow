package com.priorauth.workflow;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AuditLog {

    public record AuditEvent(
            String requestId,
            Instant timestamp,
            String actor,
            String eventType,
            String reason) {}

    private final List<AuditEvent> events = new ArrayList<>();

    public synchronized void record(
            String requestId,
            Instant timestamp,
            String actor,
            String eventType,
            String reason) {

        events.add(new AuditEvent(
                requireText(requestId, "Request ID"),
                Objects.requireNonNull(timestamp, "Timestamp is required"),
                requireText(actor, "Actor"),
                requireText(eventType, "Event type"),
                requireText(reason, "Reason")));
    }

    public synchronized List<AuditEvent> getEvents() {
        return List.copyOf(events);
    }

    public synchronized List<AuditEvent> getEventsForRequest(
            String requestId) {
        requireText(requestId, "Request ID");
        return events.stream()
                .filter(event -> event.requestId().equals(requestId))
                .toList();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank");
        }
        return value.trim();
    }
}