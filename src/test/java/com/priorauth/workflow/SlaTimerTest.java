package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SlaTimerTest {

    private final Instant start = Instant.parse("2026-10-26T09:00:00Z");

    @Test
    void standardRequestHasFourteenDaySla() {
        SlaTimer timer = new SlaTimer(RequestPriority.STANDARD, start);

        assertEquals(Duration.ofDays(14), timer.getAllowedDuration());
    }

    @Test
    void urgentRequestHasSeventyTwoHourSla() {
        SlaTimer timer = new SlaTimer(RequestPriority.URGENT, start);

        assertEquals(Duration.ofHours(72), timer.getAllowedDuration());
    }

    @Test
    void timerPausesAndResumesWithoutCountingPausedTime() {
        SlaTimer timer = new SlaTimer(RequestPriority.STANDARD, start);

        Instant pauseAt = start.plus(Duration.ofDays(3));
        timer.pause(pauseAt);

        Instant documentsArrive = start.plus(Duration.ofDays(7));
        assertEquals(Duration.ofDays(3), timer.getElapsed(documentsArrive));

        timer.resume(documentsArrive);

        Instant afterResume = documentsArrive.plus(Duration.ofDays(2));
        assertEquals(Duration.ofDays(5), timer.getElapsed(afterResume));
    }

    @Test
    void escalationIsDueAtEightyPercent() {
        SlaTimer timer = new SlaTimer(RequestPriority.STANDARD, start);

        assertFalse(timer.isEscalationDue(start.plus(Duration.ofDays(15))));
        assertTrue(timer.isEscalationDue(start.plus(Duration.ofDays(16))));
    }

    @Test
    void breachIsDetectedWhenAllowedDurationExpires() {
        SlaTimer timer = new SlaTimer(RequestPriority.URGENT, start);

        assertFalse(timer.isBreached(start.plus(Duration.ofHours(71))));
        assertTrue(timer.isBreached(start.plus(Duration.ofHours(72))));
    }

    @Test
    void pausedTimeDoesNotIncreaseElapsedDuration() {
        SlaTimer timer = new SlaTimer(RequestPriority.STANDARD, start);

        timer.pause(start.plus(Duration.ofDays(2)));

        assertEquals(
                Duration.ofDays(2),
                timer.getElapsed(start.plus(Duration.ofDays(10))));
    }

    @Test
    void standardTimerExcludesWeekendTime() {
        SlaTimer timer = new SlaTimer(RequestPriority.STANDARD, start);

        assertEquals(
                Duration.ofDays(5),
                timer.getElapsed(start.plus(Duration.ofDays(7))));
    }

    @Test
    void urgentTimerContinuesCountingWeekendTime() {
        SlaTimer timer = new SlaTimer(RequestPriority.URGENT, start);

        assertEquals(
                Duration.ofDays(7),
                timer.getElapsed(start.plus(Duration.ofDays(7))));
    }
}