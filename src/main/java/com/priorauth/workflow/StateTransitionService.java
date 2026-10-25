package com.priorauth.workflow;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class StateTransitionService {

    private final Map<AuthStatus, Set<AuthStatus>> allowedTransitions =
            new EnumMap<>(AuthStatus.class);

    public StateTransitionService() {
        allowedTransitions.put(
                AuthStatus.SUBMITTED,
                EnumSet.of(AuthStatus.UNDER_REVIEW));

        allowedTransitions.put(
                AuthStatus.UNDER_REVIEW,
                EnumSet.of(
                        AuthStatus.INFO_REQUESTED,
                        AuthStatus.APPROVED,
                        AuthStatus.DENIED));

        allowedTransitions.put(
                AuthStatus.INFO_REQUESTED,
                EnumSet.of(
                        AuthStatus.UNDER_REVIEW,
                        AuthStatus.APPROVED,
                        AuthStatus.DENIED,
                        AuthStatus.CLOSED_INSUFFICIENT_INFO));

        allowedTransitions.put(
                AuthStatus.DENIED,
                EnumSet.of(AuthStatus.APPEALED));

        allowedTransitions.put(
                AuthStatus.APPEALED,
                EnumSet.of(AuthStatus.APPEAL_REVIEW));

        allowedTransitions.put(
                AuthStatus.APPEAL_REVIEW,
                EnumSet.of(
                        AuthStatus.OVERTURNED,
                        AuthStatus.UPHELD));
    }

    public boolean canTransition(AuthStatus from, AuthStatus to) {
        if (from == null || to == null) {
            return false;
        }

        return allowedTransitions
                .getOrDefault(from, Set.of())
                .contains(to);
    }

    public void validateTransition(AuthStatus from, AuthStatus to) {
        if (!canTransition(from, to)) {
            throw new IllegalStateException(
                    "Illegal authorization transition: "
                            + from + " -> " + to);
        }
    }

    public void validateAutomaticApprovalTransition(
            AuthStatus from, AuthStatus to) {
        if (from != AuthStatus.SUBMITTED
                || to != AuthStatus.APPROVED) {
            throw new IllegalStateException(
                    "Illegal automatic approval transition: "
                            + from + " -> " + to);
        }
    }
}