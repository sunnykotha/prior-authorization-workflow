package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SlaDeadlineCalculatorTest {

    private final ZoneId zone = ZoneId.of("Asia/Kolkata");

    @Test
    void standardDeadlineSkipsWeekendsAndHolidays() {
        BusinessCalendar calendar = new BusinessCalendar(
                Set.of(LocalDate.of(2026, 10, 27)));
        SlaDeadlineCalculator calculator =
                new SlaDeadlineCalculator(calendar, zone);

        Instant submitted = Instant.parse("2026-10-23T05:00:00Z");

        Instant deadline = calculator.calculateDeadline(
                submitted, RequestPriority.STANDARD);

        assertEquals(
                Instant.parse("2026-11-13T05:00:00Z"), deadline);
    }

    @Test
    void urgentDeadlineIsExactly72ElapsedHours() {
        SlaDeadlineCalculator calculator =
                new SlaDeadlineCalculator(
                        new BusinessCalendar(Set.of()), zone);

        Instant submitted = Instant.parse("2026-10-23T05:00:00Z");

        assertEquals(
                submitted.plusSeconds(72L * 60 * 60),
                calculator.calculateDeadline(
                        submitted, RequestPriority.URGENT));
    }

    @Test
    void standardDeadlinePreservesSubmissionTime() {
        SlaDeadlineCalculator calculator =
                new SlaDeadlineCalculator(
                        new BusinessCalendar(Set.of()), zone);

        Instant submitted = Instant.parse("2026-10-23T07:15:00Z");
        Instant deadline = calculator.calculateDeadline(
                submitted, RequestPriority.STANDARD);

        assertEquals(
                submitted.atZone(zone).toLocalTime(),
                deadline.atZone(zone).toLocalTime());
    }

    @Test
    void rejectsMissingSubmissionTime() {
        SlaDeadlineCalculator calculator =
                new SlaDeadlineCalculator(
                        new BusinessCalendar(Set.of()), zone);

        assertThrows(NullPointerException.class,
                () -> calculator.calculateDeadline(
                        null, RequestPriority.STANDARD));
    }

    @Test
    void rejectsMissingPriority() {
        SlaDeadlineCalculator calculator =
                new SlaDeadlineCalculator(
                        new BusinessCalendar(Set.of()), zone);

        assertThrows(NullPointerException.class,
                () -> calculator.calculateDeadline(
                        Instant.parse("2026-10-23T05:00:00Z"), null));
    }
}