package com.priorauth.workflow;

public class AuthorizationPermissionService {

    private final StateTransitionService transitionService =
            new StateTransitionService();

    public boolean canPerform(
            ReviewerRole role,
            AuthStatus from,
            AuthStatus to,
            String actorId,
            String originalReviewerId) {

        if (role == null || actorId == null || actorId.isBlank()) {
            return false;
        }

        if (!transitionService.canTransition(from, to)) {
            return false;
        }

        if (to == AuthStatus.DENIED) {
            return role == ReviewerRole.MEDICAL_DIRECTOR;
        }

        if (from == AuthStatus.DENIED && to == AuthStatus.APPEALED) {
            return role == ReviewerRole.INTAKE_STAFF;
        }

        if (from == AuthStatus.SUBMITTED
                && to == AuthStatus.UNDER_REVIEW) {
            return role == ReviewerRole.INTAKE_STAFF
                    || role == ReviewerRole.NURSE_REVIEWER;
        }

        if (from == AuthStatus.UNDER_REVIEW
                && to == AuthStatus.APPROVED) {
            return role == ReviewerRole.NURSE_REVIEWER
                    || role == ReviewerRole.MEDICAL_DIRECTOR;
        }

        if (from == AuthStatus.UNDER_REVIEW
                && to == AuthStatus.INFO_REQUESTED) {
            return role == ReviewerRole.NURSE_REVIEWER
                    || role == ReviewerRole.MEDICAL_DIRECTOR;
        }

        if (from == AuthStatus.INFO_REQUESTED
                && to == AuthStatus.UNDER_REVIEW) {
            return role == ReviewerRole.INTAKE_STAFF
                    || role == ReviewerRole.NURSE_REVIEWER;
        }

        if (from == AuthStatus.INFO_REQUESTED
                && to == AuthStatus.APPROVED) {
            return role == ReviewerRole.NURSE_REVIEWER
                    || role == ReviewerRole.MEDICAL_DIRECTOR;
        }

        if (from == AuthStatus.APPEALED
                && to == AuthStatus.APPEAL_REVIEW) {
            return isIndependentAppealReviewer(
                    role, actorId, originalReviewerId);
        }

        if (from == AuthStatus.APPEAL_REVIEW
                && (to == AuthStatus.OVERTURNED
                || to == AuthStatus.UPHELD)) {
            return isIndependentAppealReviewer(
                    role, actorId, originalReviewerId);
        }

        return false;
    }

    private boolean isIndependentAppealReviewer(
            ReviewerRole role,
            String actorId,
            String originalReviewerId) {

        return role == ReviewerRole.APPEAL_REVIEWER
                && originalReviewerId != null
                && !originalReviewerId.isBlank()
                && !actorId.trim().equalsIgnoreCase(
                        originalReviewerId.trim());
    }

    public void validatePermission(
            ReviewerRole role,
            AuthStatus from,
            AuthStatus to,
            String actorId,
            String originalReviewerId) {

        if (!canPerform(role, from, to, actorId, originalReviewerId)) {
            throw new SecurityException(
                    "Actor is not permitted to perform transition: "
                            + from + " -> " + to);
        }
    }
}