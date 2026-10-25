package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateRequestDetectorTest {

    @Test
    void identifiesPreviouslyRegisteredSubmission() {
        DuplicateRequestDetector detector = new DuplicateRequestDetector();
        detector.register("PA-1", "PAT-1", "MRI", "EP-1");

        assertTrue(detector.isDuplicate("PAT-1", "MRI", "EP-1"));
        assertEquals("PA-1",
                detector.findDuplicateRequestId("PAT-1", "MRI", "EP-1"));
        assertEquals(1, detector.getRegisteredCount());
    }

    @Test
    void rejectsDuplicateSubmissionWithoutChangingRegistry() {
        DuplicateRequestDetector detector = new DuplicateRequestDetector();
        detector.register("PA-1", "PAT-1", "MRI", "EP-1");

        assertThrows(IllegalStateException.class, () ->
                detector.register("PA-2", "PAT-1", "MRI", "EP-1"));

        assertEquals(1, detector.getRegisteredCount());
        assertEquals("PA-1",
                detector.findDuplicateRequestId("PAT-1", "MRI", "EP-1"));
    }

    @Test
    void allowsDifferentPatientProcedureOrEpisode() {
        DuplicateRequestDetector detector = new DuplicateRequestDetector();
        detector.register("PA-1", "PAT-1", "MRI", "EP-1");

        assertFalse(detector.isDuplicate("PAT-2", "MRI", "EP-1"));
        assertFalse(detector.isDuplicate("PAT-1", "CT", "EP-1"));
        assertFalse(detector.isDuplicate("PAT-1", "MRI", "EP-2"));

        detector.register("PA-2", "PAT-1", "MRI", "EP-2");
        assertEquals(2, detector.getRegisteredCount());
    }

    @Test
    void rejectsReusedRequestIdEvenForDifferentSubmission() {
        DuplicateRequestDetector detector = new DuplicateRequestDetector();
        detector.register("PA-1", "PAT-1", "MRI", "EP-1");

        assertThrows(IllegalStateException.class, () ->
                detector.register("PA-1", "PAT-2", "CT", "EP-2"));

        assertEquals(1, detector.getRegisteredCount());
    }

    @Test
    void rejectsBlankIdentifiersAndMatchingFields() {
        DuplicateRequestDetector detector = new DuplicateRequestDetector();

        assertThrows(IllegalArgumentException.class, () ->
                detector.register(" ", "PAT-1", "MRI", "EP-1"));
        assertThrows(IllegalArgumentException.class, () ->
                detector.register("PA-1", " ", "MRI", "EP-1"));
        assertThrows(IllegalArgumentException.class, () ->
                detector.register("PA-1", "PAT-1", " ", "EP-1"));
        assertThrows(IllegalArgumentException.class, () ->
                detector.register("PA-1", "PAT-1", "MRI", " "));

        assertEquals(0, detector.getRegisteredCount());
    }

    @Test
    void trimsIdentifiersConsistently() {
        DuplicateRequestDetector detector = new DuplicateRequestDetector();
        detector.register(" PA-1 ", " PAT-1 ", " MRI ", " EP-1 ");

        assertTrue(detector.isDuplicate("PAT-1", "MRI", "EP-1"));
        assertEquals("PA-1",
                detector.findDuplicateRequestId(" PAT-1 ", " MRI ", " EP-1 "));
    }
}