package com.priorauth.workflow;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AutoApprovalService {
    private static final String SYSTEM_ACTOR = "AUTO_APPROVAL";

    private final Set<String> lowRiskProcedureCodes;
    private final AuditLog auditLog;
    private final StateTransitionService transitionService;

    public AutoApprovalService(
            Set<String> lowRiskProcedureCodes,
            AuditLog auditLog) {
        this(lowRiskProcedureCodes, auditLog, new StateTransitionService());
    }

    public AutoApprovalService(
            Set<String> lowRiskProcedureCodes,
            AuditLog auditLog,
            StateTransitionService transitionService) {

        Objects.requireNonNull(
                lowRiskProcedureCodes, "Low-risk codes are required");
        this.auditLog = Objects.requireNonNull(
                auditLog, "Audit log is required");
        this.transitionService = Objects.requireNonNull(
                transitionService, "Transition service is required");

        this.lowRiskProcedureCodes = lowRiskProcedureCodes.stream()
                .map(code -> {
                    if (code == null || code.isBlank()) {
                        throw new IllegalArgumentException(
                                "Procedure codes must not be blank");
                    }
                    return code.trim();
                })
                .collect(Collectors.toUnmodifiableSet());
    }

    public AuthStatus evaluate(
            AuthorizationRequest request,
            AuthStatus currentStatus,
            Instant timestamp) {

        Objects.requireNonNull(request, "Request is required");
        Objects.requireNonNull(currentStatus, "Current status is required");
        Objects.requireNonNull(timestamp, "Timestamp is required");

        if (currentStatus != AuthStatus.SUBMITTED) {
            return currentStatus;
        }

        if (!lowRiskProcedureCodes.contains(request.getProcedureCode())) {
            return currentStatus;
        }

        if (!request.isDocumentationComplete()) {
            return currentStatus;
        }

        transitionService.validateAutomaticApprovalTransition(
                currentStatus, AuthStatus.APPROVED);

        auditLog.record(
                request.getId(),
                timestamp,
                SYSTEM_ACTOR,
                "AUTO_APPROVED",
                "Low-risk procedure with complete documentation");

        return AuthStatus.APPROVED;
    }
}