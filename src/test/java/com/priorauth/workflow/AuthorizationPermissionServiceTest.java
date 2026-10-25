package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationPermissionServiceTest {

    private final AuthorizationPermissionService service =
            new AuthorizationPermissionService();

    @Test
    void nurseCanApproveRoutineRequest() {
        assertTrue(service.canPerform(
                ReviewerRole.NURSE_REVIEWER,
                AuthStatus.UNDER_REVIEW,
                AuthStatus.APPROVED,
                "nurse-101",
                null));
    }

    @Test
    void nurseCannotDenyRequest() {
        assertFalse(service.canPerform(
                ReviewerRole.NURSE_REVIEWER,
                AuthStatus.UNDER_REVIEW,
                AuthStatus.DENIED,
                "nurse-101",
                null));
    }

    @Test
    void onlyMedicalDirectorCanDenyRequest() {
        assertTrue(service.canPerform(
                ReviewerRole.MEDICAL_DIRECTOR,
                AuthStatus.UNDER_REVIEW,
                AuthStatus.DENIED,
                "director-101",
                null));

        assertFalse(service.canPerform(
                ReviewerRole.INTAKE_STAFF,
                AuthStatus.UNDER_REVIEW,
                AuthStatus.DENIED,
                "intake-101",
                null));
    }

    @Test
    void differentReviewerCanReviewAppeal() {
        assertTrue(service.canPerform(
                ReviewerRole.APPEAL_REVIEWER,
                AuthStatus.APPEALED,
                AuthStatus.APPEAL_REVIEW,
                "reviewer-202",
                "reviewer-101"));
    }

    @Test
    void originalReviewerCannotReviewOwnAppeal() {
        assertFalse(service.canPerform(
                ReviewerRole.APPEAL_REVIEWER,
                AuthStatus.APPEALED,
                AuthStatus.APPEAL_REVIEW,
                "reviewer-101",
                "reviewer-101"));
    }

    @Test
    void invalidTransitionIsRejectedEvenForMedicalDirector() {
        assertFalse(service.canPerform(
                ReviewerRole.MEDICAL_DIRECTOR,
                AuthStatus.SUBMITTED,
                AuthStatus.APPROVED,
                "director-101",
                null));
    }

    @Test
    void blankActorIsRejected() {
        assertFalse(service.canPerform(
                ReviewerRole.NURSE_REVIEWER,
                AuthStatus.UNDER_REVIEW,
                AuthStatus.APPROVED,
                " ",
                null));
    }
}