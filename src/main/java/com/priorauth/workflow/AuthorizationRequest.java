package com.priorauth.workflow;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AuthorizationRequest {
    private final String id;
    private final String procedureCode;
    private final RequestPriority priority;
    private final Instant submittedAt;
    private final Set<String> requiredDocuments;
    private final Set<String> receivedDocuments = new HashSet<>();

    public AuthorizationRequest(
            String id,
            String procedureCode,
            RequestPriority priority,
            Instant submittedAt,
            Set<String> requiredDocuments) {

        this.id = requireText(id, "Request ID");
        this.procedureCode = requireText(
                procedureCode, "Procedure code");
        this.priority = Objects.requireNonNull(
                priority, "Priority is required");
        this.submittedAt = Objects.requireNonNull(
                submittedAt, "Submission time is required");
        Objects.requireNonNull(
                requiredDocuments, "Required documents are required");

        this.requiredDocuments = new HashSet<>();
        for (String document : requiredDocuments) {
            this.requiredDocuments.add(
                    requireText(document, "Required document"));
        }
    }

    public synchronized void receiveDocument(String document) {
        receivedDocuments.add(requireText(document, "Document"));
    }

    public synchronized boolean isDocumentationComplete() {
        return receivedDocuments.containsAll(requiredDocuments);
    }

    public String getId() {
        return id;
    }

    public String getProcedureCode() {
        return procedureCode;
    }

    public RequestPriority getPriority() {
        return priority;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Set<String> getRequiredDocuments() {
        return Set.copyOf(requiredDocuments);
    }

    public synchronized Set<String> getReceivedDocuments() {
        return Set.copyOf(receivedDocuments);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank");
        }
        return value.trim();
    }
}