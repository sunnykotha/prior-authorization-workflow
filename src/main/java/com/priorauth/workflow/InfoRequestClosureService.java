package com.priorauth.workflow;

import java.time.Instant;
import java.util.Objects;

public class InfoRequestClosureService {
    private final AuditLog auditLog;

    public InfoRequestClosureService(AuditLog auditLog) {
        this.auditLog = Objects.requireNonNull(
                auditLog, "Audit log is required");
    }

    public AuthStatus evaluate(
            String requestId,
            String actor,
            AuthStatus currentStatus,
            Instant informationRequestedAt,
            int allowedDays,
            Instant now) {

        requireText(requestId, "Request ID");
        requireText(actor, "Actor");
        Objects.requireNonNull(
                currentStatus, "Current status is required");
        Objects.requireNonNull(
                informationRequestedAt, "Information request time is required");
        Objects.requireNonNull(now, "Current time is required");

        if (allowedDays <= 0) {
            throw new IllegalArgumentException(
                    "Allowed days must be greater than zero");
        }

        if (now.isBefore(informationRequestedAt)) {
            throw new IllegalArgumentException(
                    "Current time cannot be before the information request");
        }

        if (currentStatus != AuthStatus.INFO_REQUESTED) {
            return currentStatus;
        }

        Instant deadline = informationRequestedAt.plusSeconds(
                allowedDays * 24L * 60 * 60);

        if (now.isBefore(deadline)) {
            return currentStatus;
        }

        auditLog.record(
                requestId,
                now,
                actor,
                "CLOSED_INSUFFICIENT_INFO",
                "Requested information was not received by the deadline");

        return AuthStatus.CLOSED_INSUFFICIENT_INFO;
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank");
        }
    }
}