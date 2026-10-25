package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationSubmissionWorkflowTest {

    @Test
    void acceptsFirstSubmissionAndMakesItDiscoverable() {
        AuthorizationSubmissionWorkflow workflow =
                new AuthorizationSubmissionWorkflow(
                        new DuplicateRequestDetector());

        String result = workflow.submit(
                " PA-SUB-1 ", " PAT-1 ", " MRI ", " EP-1 ");

        assertEquals("PA-SUB-1", result);
        assertTrue(workflow.isDuplicate("PAT-1", "MRI", "EP-1"));
        assertEquals("PA-SUB-1",
                workflow.findExistingRequestId("PAT-1", "MRI", "EP-1"));
        assertEquals(1, workflow.getSubmittedCount());
    }

    @Test
    void rejectsDuplicateBeforeAddingAnotherSubmission() {
        AuthorizationSubmissionWorkflow workflow =
                new AuthorizationSubmissionWorkflow(
                        new DuplicateRequestDetector());

        workflow.submit("PA-SUB-1", "PAT-1", "MRI", "EP-1");

        assertThrows(IllegalStateException.class, () ->
                workflow.submit("PA-SUB-2", "PAT-1", "MRI", "EP-1"));

        assertEquals(1, workflow.getSubmittedCount());
        assertEquals("PA-SUB-1",
                workflow.findExistingRequestId("PAT-1", "MRI", "EP-1"));
    }

    @Test
    void rejectsReusedRequestIdForDifferentSubmission() {
        AuthorizationSubmissionWorkflow workflow =
                new AuthorizationSubmissionWorkflow(
                        new DuplicateRequestDetector());

        workflow.submit("PA-SUB-1", "PAT-1", "MRI", "EP-1");

        assertThrows(IllegalStateException.class, () ->
                workflow.submit("PA-SUB-1", "PAT-2", "CT", "EP-2"));

        assertEquals(1, workflow.getSubmittedCount());
        assertFalse(workflow.isDuplicate("PAT-2", "CT", "EP-2"));
    }
}