package com.priorauth.workflow;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BusinessCalendar {

    private final Set<LocalDate> holidays = new HashSet<>();

    public BusinessCalendar(Set<LocalDate> holidays) {
        if (holidays != null) {
            this.holidays.addAll(holidays);
        }
    }

    public boolean isBusinessDay(LocalDate date) {
        Objects.requireNonNull(date, "Date is required");

        DayOfWeek day = date.getDayOfWeek();

        return day != DayOfWeek.SATURDAY
                && day != DayOfWeek.SUNDAY
                && !holidays.contains(date);
    }

    public LocalDate addBusinessDays(LocalDate startDate, int days) {
        Objects.requireNonNull(startDate, "Start date is required");

        if (days < 0) {
            throw new IllegalArgumentException(
                    "Number of business days cannot be negative");
        }

        LocalDate result = startDate;
        int added = 0;

        while (added < days) {
            result = result.plusDays(1);

            if (isBusinessDay(result)) {
                added++;
            }
        }

        return result;
    }

    public long countBusinessDays(
            LocalDate startInclusive,
            LocalDate endExclusive) {

        Objects.requireNonNull(startInclusive, "Start date is required");
        Objects.requireNonNull(endExclusive, "End date is required");

        if (endExclusive.isBefore(startInclusive)) {
            throw new IllegalArgumentException(
                    "End date cannot be before start date");
        }

        long count = 0;
        LocalDate date = startInclusive;

        while (date.isBefore(endExclusive)) {
            if (isBusinessDay(date)) {
                count++;
            }

            date = date.plusDays(1);
        }

        return count;
    }
}