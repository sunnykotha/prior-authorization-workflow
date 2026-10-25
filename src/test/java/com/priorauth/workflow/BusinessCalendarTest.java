package com.priorauth.workflow;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BusinessCalendarTest {

    private final LocalDate holiday = LocalDate.of(2026, 10, 27);

    private final BusinessCalendar calendar =
            new BusinessCalendar(Set.of(holiday));

    @Test
    void shouldExcludeSaturdayAndSunday() {
        assertFalse(calendar.isBusinessDay(LocalDate.of(2026, 10, 24)));
        assertFalse(calendar.isBusinessDay(LocalDate.of(2026, 10, 25)));
        assertTrue(calendar.isBusinessDay(LocalDate.of(2026, 10, 26)));
    }

    @Test
    void shouldExcludeConfiguredHolidays() {
        assertFalse(calendar.isBusinessDay(holiday));
    }

    @Test
    void shouldAddBusinessDaysAndSkipWeekendsAndHolidays() {
        LocalDate friday = LocalDate.of(2026, 10, 23);

        assertEquals(
                LocalDate.of(2026, 10, 26),
                calendar.addBusinessDays(friday, 1));

        assertEquals(
                LocalDate.of(2026, 10, 28),
                calendar.addBusinessDays(friday, 2));
    }

    @Test
    void shouldCountBusinessDaysExcludingEndDate() {
        LocalDate friday = LocalDate.of(2026, 10, 23);
        LocalDate wednesday = LocalDate.of(2026, 10, 28);

        // Friday and Monday count. Tuesday is a configured holiday.
        assertEquals(
                2,
                calendar.countBusinessDays(friday, wednesday));
    }

    @Test
    void zeroBusinessDaysShouldReturnStartDate() {
        LocalDate date = LocalDate.of(2026, 10, 23);

        assertEquals(date, calendar.addBusinessDays(date, 0));
    }

    @Test
    void shouldRejectNegativeBusinessDays() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calendar.addBusinessDays(
                        LocalDate.of(2026, 10, 23), -1));
    }

    @Test
    void shouldRejectReversedDateRange() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calendar.countBusinessDays(
                        LocalDate.of(2026, 10, 28),
                        LocalDate.of(2026, 10, 23)));
    }
}