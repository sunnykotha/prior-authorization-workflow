package com.priorauth.workflow;

import java.time.Instant;
import java.util.Objects;

/**
 * Records SLA escalation and breach alerts in the append-only audit log.
 * Each alert type is recorded at most once per reporter instance.
 */
public class SlaAlertReporter {

    private final String requestId;
    private final String actor;
    private final SlaTimer timer;
    private final SlaMonitor monitor;
    private final AuditLog auditLog;

    private boolean escalationReported;
    private boolean breachReported;

    public SlaAlertReporter(
            String requestId,
            String actor,
            SlaTimer timer,
            SlaMonitor monitor,
            AuditLog auditLog) {
        this.requestId = requireText(requestId, "Request ID");
        this.actor = requireText(actor, "Actor");
        this.timer = Objects.requireNonNull(timer, "SLA timer is required");
        this.monitor = Objects.requireNonNull(monitor, "SLA monitor is required");
        this.auditLog = Objects.requireNonNull(auditLog, "Audit log is required");
    }

    public synchronized SlaMonitor.Assessment check(Instant timestamp) {
        Objects.requireNonNull(timestamp, "Timestamp is required");

        var elapsed = timer.getElapsed(timestamp);
        var allowed = timer.getAllowedDuration();
        var assessment = monitor.assess(elapsed, allowed);

        if (assessment.status() == SlaMonitor.Status.BREACHED) {
            if (!escalationReported) {
                auditLog.record(
                        requestId, timestamp, actor,
                        "SLA_ESCALATION",
                        "SLA reached the 80% escalation threshold");
                escalationReported = true;
            }

            if (!breachReported) {
                auditLog.record(
                        requestId, timestamp, actor,
                        "SLA_BREACH",
                        "SLA reached or exceeded 100% of allowed review time");
                breachReported = true;
            }
        } else if (assessment.status() == SlaMonitor.Status.ESCALATION_REQUIRED
                && !escalationReported) {
            auditLog.record(
                    requestId, timestamp, actor,
                    "SLA_ESCALATION",
                    "SLA reached the 80% escalation threshold");
            escalationReported = true;
        }

        return assessment;
    }

    public synchronized boolean isEscalationReported() {
        return escalationReported;
    }

    public synchronized boolean isBreachReported() {
        return breachReported;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}