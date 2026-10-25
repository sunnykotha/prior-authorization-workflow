package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StateTransitionServiceTest {

    private final StateTransitionService service =
            new StateTransitionService();

    @Test
    void shouldAllowSubmissionToReview() {
        assertTrue(service.canTransition(
                AuthStatus.SUBMITTED,
                AuthStatus.UNDER_REVIEW));
    }

    @Test
    void shouldAllowInformationRequestAndResumeReview() {
        assertTrue(service.canTransition(
                AuthStatus.UNDER_REVIEW,
                AuthStatus.INFO_REQUESTED));

        assertTrue(service.canTransition(
                AuthStatus.INFO_REQUESTED,
                AuthStatus.UNDER_REVIEW));
    }

    @Test
    void shouldAllowApprovalAndDenialFromReview() {
        assertTrue(service.canTransition(
                AuthStatus.UNDER_REVIEW,
                AuthStatus.APPROVED));

        assertTrue(service.canTransition(
                AuthStatus.UNDER_REVIEW,
                AuthStatus.DENIED));
    }

    @Test
    void shouldAllowAppealAndAppealDecision() {
        assertTrue(service.canTransition(
                AuthStatus.DENIED,
                AuthStatus.APPEALED));

        assertTrue(service.canTransition(
                AuthStatus.APPEALED,
                AuthStatus.APPEAL_REVIEW));

        assertTrue(service.canTransition(
                AuthStatus.APPEAL_REVIEW,
                AuthStatus.OVERTURNED));

        assertTrue(service.canTransition(
                AuthStatus.APPEAL_REVIEW,
                AuthStatus.UPHELD));
    }

    @Test
    void shouldRejectIllegalTransitions() {
        assertFalse(service.canTransition(
                AuthStatus.SUBMITTED,
                AuthStatus.APPROVED));

        assertFalse(service.canTransition(
                AuthStatus.APPROVED,
                AuthStatus.APPEALED));

        assertFalse(service.canTransition(
                AuthStatus.UPHELD,
                AuthStatus.APPROVED));
    }

    @Test
    void shouldThrowExceptionForIllegalTransition() {
        assertThrows(
                IllegalStateException.class,
                () -> service.validateTransition(
                        AuthStatus.SUBMITTED,
                        AuthStatus.APPROVED));
    }
}