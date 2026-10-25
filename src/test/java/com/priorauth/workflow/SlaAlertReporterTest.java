package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SlaAlertReporterTest {

    private final Instant start = Instant.parse("2026-10-26T09:00:00Z");

    private SlaAlertReporter createReporter(
            SlaTimer timer, AuditLog auditLog) {
        return new SlaAlertReporter(
                "PA-ALERT-1", "sla-monitor", timer,
                new SlaMonitor(), auditLog);
    }

    @Test
    void reportsEscalationAtEightyPercentOnlyOnce() {
        SlaTimer timer = new SlaTimer(RequestPriority.STANDARD, start);
        AuditLog log = new AuditLog();
        SlaAlertReporter reporter = createReporter(timer, log);
        Instant threshold = atElapsedThreshold(
                timer, timer.getAllowedDuration().multipliedBy(4).dividedBy(5));

        var assessment = reporter.check(threshold);
        reporter.check(threshold.plusSeconds(60));

        assertEquals(SlaMonitor.Status.ESCALATION_REQUIRED, assessment.status());
        assertTrue(reporter.isEscalationReported());
        assertFalse(reporter.isBreachReported());
        assertEquals(1, log.getEventsForRequest("PA-ALERT-1").size());
        assertEquals("SLA_ESCALATION",
                log.getEventsForRequest("PA-ALERT-1").get(0).eventType());
    }

    @Test
    void reportsEscalationAndBreachWhenFirstCheckedAtOneHundredPercent() {
        SlaTimer timer = new SlaTimer(RequestPriority.STANDARD, start);
        AuditLog log = new AuditLog();
        SlaAlertReporter reporter = createReporter(timer, log);
        Instant deadline = atElapsedThreshold(timer, timer.getAllowedDuration());

        var assessment = reporter.check(deadline);
        reporter.check(deadline.plusSeconds(60));

        assertEquals(SlaMonitor.Status.BREACHED, assessment.status());
        assertTrue(reporter.isEscalationReported());
        assertTrue(reporter.isBreachReported());

        var events = log.getEventsForRequest("PA-ALERT-1");
        assertEquals(2, events.size());
        assertEquals("SLA_ESCALATION", events.get(0).eventType());
        assertEquals("SLA_BREACH", events.get(1).eventType());
    }

    @Test
    void doesNotReportAlertsBeforeEscalationThreshold() {
        SlaTimer timer = new SlaTimer(RequestPriority.STANDARD, start);
        AuditLog log = new AuditLog();
        SlaAlertReporter reporter = createReporter(timer, log);

        var assessment = reporter.check(
                start.plus(timer.getAllowedDuration().dividedBy(2)));

        assertEquals(SlaMonitor.Status.NORMAL, assessment.status());
        assertFalse(reporter.isEscalationReported());
        assertFalse(reporter.isBreachReported());
        assertTrue(log.getEventsForRequest("PA-ALERT-1").isEmpty());
    }

    @Test
    void pausedTimeDoesNotTriggerEscalationOrBreach() {
        SlaTimer timer = new SlaTimer(RequestPriority.STANDARD, start);
        AuditLog log = new AuditLog();
        SlaAlertReporter reporter = createReporter(timer, log);

        Instant pauseAt = start.plus(Duration.ofDays(2));
        timer.pause(pauseAt);

        var assessment = reporter.check(pauseAt.plus(Duration.ofDays(30)));

        assertEquals(SlaMonitor.Status.NORMAL, assessment.status());
        assertFalse(reporter.isEscalationReported());
        assertFalse(reporter.isBreachReported());
        assertTrue(log.getEventsForRequest("PA-ALERT-1").isEmpty());
    }

    private Instant atElapsedThreshold(SlaTimer timer, Duration target) {
        Instant timestamp = start;
        for (int hour = 0; hour <= 24 * 40; hour++) {
            if (timer.getElapsed(timestamp).compareTo(target) >= 0) {
                return timestamp;
            }
            timestamp = timestamp.plusSeconds(3600);
        }
        throw new AssertionError("SLA threshold not reached within 40 days");
    }
}