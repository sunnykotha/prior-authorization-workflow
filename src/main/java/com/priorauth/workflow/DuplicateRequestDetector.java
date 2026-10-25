package com.priorauth.workflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Detects duplicate submissions using patient, procedure, and episode.
 * This detector is in-memory and scoped to its lifetime.
 */
public final class DuplicateRequestDetector {

    public record SubmissionKey(
            String patientId,
            String procedureCode,
            String episodeId) { }

    private final Map<SubmissionKey, String> registeredRequests =
            new HashMap<>();
    private final Set<String> registeredRequestIds = new HashSet<>();

    /**
     * Returns true when the matching submission has already been registered.
     */
    public synchronized boolean isDuplicate(
            String patientId, String procedureCode, String episodeId) {
        return registeredRequests.containsKey(
                createKey(patientId, procedureCode, episodeId));
    }

    /**
     * Registers a submission. Duplicate keys and request IDs are rejected.
     */
    public synchronized void register(
            String requestId,
            String patientId,
            String procedureCode,
            String episodeId) {
        String validRequestId = requireText(requestId, "Request ID");
        SubmissionKey key = createKey(patientId, procedureCode, episodeId);

        if (registeredRequestIds.contains(validRequestId)) {
            throw new IllegalStateException(
                    "Request ID is already registered: " + validRequestId);
        }

        if (registeredRequests.containsKey(key)) {
            throw new IllegalStateException(
                    "Duplicate authorization submission detected");
        }

        registeredRequests.put(key, validRequestId);
        registeredRequestIds.add(validRequestId);
    }

    /**
     * Returns the existing request ID for a matching submission, if any.
     */
    public synchronized String findDuplicateRequestId(
            String patientId, String procedureCode, String episodeId) {
        return registeredRequests.get(
                createKey(patientId, procedureCode, episodeId));
    }

    public synchronized int getRegisteredCount() {
        return registeredRequests.size();
    }

    private static SubmissionKey createKey(
            String patientId, String procedureCode, String episodeId) {
        return new SubmissionKey(
                requireText(patientId, "Patient ID"),
                requireText(procedureCode, "Procedure code"),
                requireText(episodeId, "Episode ID"));
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank");
        }
        return value.trim();
    }
}