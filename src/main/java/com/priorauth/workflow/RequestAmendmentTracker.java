package com.priorauth.workflow;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Tracks request amendments and resets the review-start timestamp.
 */
public final class RequestAmendmentTracker {

    public record Amendment(
            int revision,
            Instant timestamp,
            String actor,
            String reason) { }

    private final String requestId;
    private final Instant submittedAt;
    private final AuditLog auditLog;
    private final List<Amendment> amendments = new ArrayList<>();
    private Instant reviewStartedAt;

    public RequestAmendmentTracker(
            String requestId,
            Instant submittedAt,
            AuditLog auditLog) {
        this.requestId = requireText(requestId, "Request ID");
        this.submittedAt = Objects.requireNonNull(
                submittedAt, "Submission timestamp is required");
        this.auditLog = Objects.requireNonNull(
                auditLog, "Audit log is required");
        this.reviewStartedAt = submittedAt;
    }

    /**
     * Records an amendment and resets the review-start timestamp.
     * Amendment timestamps must not precede submission or the prior review start.
     */
    public synchronized Amendment amend(
            String actor, String reason, Instant timestamp) {
        String validActor = requireText(actor, "Actor");
        String validReason = requireText(reason, "Reason");
        Objects.requireNonNull(timestamp, "Amendment timestamp is required");

        if (timestamp.isBefore(submittedAt)) {
            throw new IllegalArgumentException(
                    "Amendment cannot predate request submission");
        }

        if (timestamp.isBefore(reviewStartedAt)) {
            throw new IllegalArgumentException(
                    "Amendment timestamp cannot move review time backward");
        }

        Amendment amendment = new Amendment(
                amendments.size() + 1,
                timestamp,
                validActor,
                validReason);

        auditLog.record(
                requestId,
                timestamp,
                validActor,
                "REQUEST_AMENDED",
                "Revision " + amendment.revision() + ": " + validReason);

        amendments.add(amendment);
        reviewStartedAt = timestamp;
        return amendment;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public synchronized Instant getReviewStartedAt() {
        return reviewStartedAt;
    }

    public synchronized int getRevision() {
        return amendments.size();
    }

    public synchronized List<Amendment> getAmendments() {
        return List.copyOf(amendments);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank");
        }
        return value.trim();
    }
}