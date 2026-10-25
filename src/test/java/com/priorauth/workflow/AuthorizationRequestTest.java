package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationRequestTest {

    private final Instant submittedAt =
            Instant.parse("2026-10-23T05:00:00Z");

    @Test
    void storesRequestDetails() {
        AuthorizationRequest request = new AuthorizationRequest(
                "PA-1001", "MRI-BRAIN", RequestPriority.STANDARD,
                submittedAt, Set.of("Clinical notes", "MRI report"));

        assertEquals("PA-1001", request.getId());
        assertEquals("MRI-BRAIN", request.getProcedureCode());
        assertEquals(RequestPriority.STANDARD, request.getPriority());
        assertEquals(submittedAt, request.getSubmittedAt());
    }

    @Test
    void documentationIsIncompleteWhenRequiredDocumentsAreMissing() {
        AuthorizationRequest request = new AuthorizationRequest(
                "PA-1002", "MRI-BRAIN", RequestPriority.STANDARD,
                submittedAt, Set.of("Clinical notes", "MRI report"));

        assertFalse(request.isDocumentationComplete());
    }

    @Test
    void documentationBecomesCompleteWhenAllRequiredDocumentsArrive() {
        AuthorizationRequest request = new AuthorizationRequest(
                "PA-1003", "MRI-BRAIN", RequestPriority.STANDARD,
                submittedAt, Set.of("Clinical notes", "MRI report"));

        request.receiveDocument("Clinical notes");
        assertFalse(request.isDocumentationComplete());

        request.receiveDocument("MRI report");
        assertTrue(request.isDocumentationComplete());
    }

    @Test
    void requestWithNoRequiredDocumentsIsComplete() {
        AuthorizationRequest request = new AuthorizationRequest(
                "PA-1004", "MRI-BRAIN", RequestPriority.URGENT,
                submittedAt, Set.of());

        assertTrue(request.isDocumentationComplete());
    }

    @Test
    void receivingTheSameDocumentTwiceDoesNotCreateDuplicates() {
        AuthorizationRequest request = new AuthorizationRequest(
                "PA-1005", "MRI-BRAIN", RequestPriority.STANDARD,
                submittedAt, Set.of("Clinical notes"));

        request.receiveDocument("Clinical notes");
        request.receiveDocument("Clinical notes");

        assertEquals(1, request.getReceivedDocuments().size());
        assertTrue(request.isDocumentationComplete());
    }

    @Test
    void receivedDocumentsDoNotChangeRequiredDocumentList() {
        AuthorizationRequest request = new AuthorizationRequest(
                "PA-1006", "MRI-BRAIN", RequestPriority.STANDARD,
                submittedAt, Set.of("Clinical notes"));

        request.receiveDocument("Extra document");

        assertEquals(Set.of("Clinical notes"),
                request.getRequiredDocuments());
        assertFalse(request.isDocumentationComplete());
    }

    @Test
    void rejectsBlankRequiredDocument() {
        assertThrows(IllegalArgumentException.class,
                () -> new AuthorizationRequest(
                        "PA-1007", "MRI-BRAIN", RequestPriority.STANDARD,
                        submittedAt, Set.of(" ")));
    }

    @Test
    void rejectsBlankReceivedDocument() {
        AuthorizationRequest request = new AuthorizationRequest(
                "PA-1008", "MRI-BRAIN", RequestPriority.STANDARD,
                submittedAt, Set.of("Clinical notes"));

        assertThrows(IllegalArgumentException.class,
                () -> request.receiveDocument(" "));
    }
}