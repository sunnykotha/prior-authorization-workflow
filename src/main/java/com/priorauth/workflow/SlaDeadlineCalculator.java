package com.priorauth.workflow;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

public class SlaDeadlineCalculator {
    private final BusinessCalendar businessCalendar;
    private final ZoneId zoneId;

    public SlaDeadlineCalculator(
            BusinessCalendar businessCalendar, ZoneId zoneId) {
        this.businessCalendar = Objects.requireNonNull(
                businessCalendar, "Business calendar is required");
        this.zoneId = Objects.requireNonNull(
                zoneId, "Time zone is required");
    }

    public Instant calculateDeadline(
            Instant submittedAt, RequestPriority priority) {
        Objects.requireNonNull(submittedAt, "Submission time is required");
        Objects.requireNonNull(priority, "Priority is required");

        if (priority == RequestPriority.URGENT) {
            return submittedAt.plusSeconds(72L * 60 * 60);
        }

        ZonedDateTime submitted = submittedAt.atZone(zoneId);
        LocalDateTime deadlineLocal = businessCalendar
                .addBusinessDays(submitted.toLocalDate(), 14)
                .atTime(submitted.toLocalTime());

        return deadlineLocal.atZone(zoneId).toInstant();
    }
}