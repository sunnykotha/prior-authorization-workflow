package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class SlaMonitorTest {

    private final SlaMonitor monitor = new SlaMonitor();
    private final Duration allowed = Duration.ofDays(14);

    @Test
    void reportsNormalBeforeEightyPercent() {
        SlaMonitor.Assessment result = monitor.assess(
                Duration.ofDays(10), allowed);

        assertEquals(SlaMonitor.Status.NORMAL, result.status());
    }

    @Test
    void escalatesAtExactlyEightyPercent() {
        SlaMonitor.Assessment result = monitor.assess(
                Duration.ofNanos(allowed.toNanos() * 4 / 5), allowed);

        assertEquals(
                SlaMonitor.Status.ESCALATION_REQUIRED, result.status());
        assertEquals(80.0, result.elapsedPercent(), 0.0001);
    }

    @Test
    void escalatesAfterEightyPercentButBeforeDeadline() {
        SlaMonitor.Assessment result = monitor.assess(
                Duration.ofDays(12), allowed);

        assertEquals(
                SlaMonitor.Status.ESCALATION_REQUIRED, result.status());
    }

    @Test
    void reportsBreachAtExactlyOneHundredPercent() {
        SlaMonitor.Assessment result = monitor.assess(allowed, allowed);

        assertEquals(SlaMonitor.Status.BREACHED, result.status());
        assertEquals(100.0, result.elapsedPercent(), 0.0001);
    }

    @Test
    void reportsBreachAfterDeadline() {
        SlaMonitor.Assessment result = monitor.assess(
                Duration.ofDays(15), allowed);

        assertEquals(SlaMonitor.Status.BREACHED, result.status());
        assertTrue(result.elapsedPercent() > 100.0);
    }

    @Test
    void rejectsNegativeElapsedTime() {
        assertThrows(IllegalArgumentException.class,
                () -> monitor.assess(Duration.ofSeconds(-1), allowed));
    }

    @Test
    void rejectsZeroAllowedTime() {
        assertThrows(IllegalArgumentException.class,
                () -> monitor.assess(
                        Duration.ofSeconds(1), Duration.ZERO));
    }

    @Test
    void rejectsMissingDurations() {
        assertThrows(NullPointerException.class,
                () -> monitor.assess(null, allowed));

        assertThrows(NullPointerException.class,
                () -> monitor.assess(Duration.ZERO, null));
    }
}