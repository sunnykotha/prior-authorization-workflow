package com.priorauth.workflow;

import java.util.Objects;

/**
 * Validates and registers authorization submissions before they enter review.
 * Duplicate detection is in-memory and lasts for this workflow's lifetime.
 */
public final class AuthorizationSubmissionWorkflow {
    private final DuplicateRequestDetector duplicateDetector;

    public AuthorizationSubmissionWorkflow(
            DuplicateRequestDetector duplicateDetector) {
        this.duplicateDetector = Objects.requireNonNull(
                duplicateDetector, "duplicateDetector");
    }

    /**
     * Registers a submission and returns its request ID.
     * Throws IllegalStateException if its request ID or submission key
     * has already been registered.
     */
    public synchronized String submit(
            String requestId,
            String patientId,
            String procedureCode,
            String episodeId) {
        duplicateDetector.register(
                requestId, patientId, procedureCode, episodeId);
        return requestId.trim();
    }

    public boolean isDuplicate(
            String patientId, String procedureCode, String episodeId) {
        return duplicateDetector.isDuplicate(
                patientId, procedureCode, episodeId);
    }

    public String findExistingRequestId(
            String patientId, String procedureCode, String episodeId) {
        return duplicateDetector.findDuplicateRequestId(
                patientId, procedureCode, episodeId);
    }

    public int getSubmittedCount() {
        return duplicateDetector.getRegisteredCount();
    }
}