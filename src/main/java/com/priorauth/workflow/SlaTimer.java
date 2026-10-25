package com.priorauth.workflow;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Set;

public class SlaTimer {
    private final Duration allowedDuration;
    private final RequestPriority priority;
    private final BusinessCalendar businessCalendar;
    private final ZoneId zoneId;
    private Instant lastStartedAt;
    private Duration elapsedDuration = Duration.ZERO;
    private boolean paused;
    private boolean breached;

    public SlaTimer(RequestPriority priority, Instant startedAt) {
        this(priority, startedAt, new BusinessCalendar(Set.of()),
                ZoneId.of("UTC"));
    }

    public SlaTimer(
            RequestPriority priority,
            Instant startedAt,
            BusinessCalendar businessCalendar,
            ZoneId zoneId) {
        this.priority = Objects.requireNonNull(
                priority, "Priority is required");
        this.lastStartedAt = Objects.requireNonNull(
                startedAt, "Start time is required");
        this.businessCalendar = Objects.requireNonNull(
                businessCalendar, "Business calendar is required");
        this.zoneId = Objects.requireNonNull(
                zoneId, "Time zone is required");
        this.allowedDuration = switch (priority) {
            case STANDARD -> Duration.ofDays(14);
            case URGENT -> Duration.ofHours(72);
        };
    }

    public void pause(Instant now) {
        Objects.requireNonNull(now, "Current time is required");
        if (!paused) {
            elapsedDuration = elapsedDuration.plus(
                    activeDuration(lastStartedAt, now));
            lastStartedAt = now;
            paused = true;
        }
        updateBreachStatus();
    }

    public void resume(Instant now) {
        Objects.requireNonNull(now, "Current time is required");
        if (paused) {
            lastStartedAt = now;
            paused = false;
        }
        updateBreachStatus();
    }

    public void restart(Instant now) {
        Objects.requireNonNull(now, "Restart time is required");
        elapsedDuration = Duration.ZERO;
        lastStartedAt = now;
        paused = false;
    }

    public Duration getElapsed(Instant now) {
        Objects.requireNonNull(now, "Current time is required");
        if (paused) {
            return elapsedDuration;
        }
        return elapsedDuration.plus(activeDuration(lastStartedAt, now));
    }

    public Duration getRemaining(Instant now) {
        Duration remaining = allowedDuration.minus(getElapsed(now));
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    public boolean isEscalationDue(Instant now) {
        Duration threshold = allowedDuration.multipliedBy(80).dividedBy(100);
        return getElapsed(now).compareTo(threshold) >= 0;
    }

    public boolean isBreached(Instant now) {
        return getElapsed(now).compareTo(allowedDuration) >= 0;
    }

    public boolean isPaused() {
        return paused;
    }

    public Duration getAllowedDuration() {
        return allowedDuration;
    }

    private Duration activeDuration(Instant start, Instant end) {
        if (!end.isAfter(start)) {
            return Duration.ZERO;
        }

        if (priority == RequestPriority.URGENT) {
            return Duration.between(start, end);
        }

        Duration total = Duration.ZERO;
        Instant cursor = start;

        while (cursor.isBefore(end)) {
            LocalDate date = cursor.atZone(zoneId).toLocalDate();
            Instant nextDay = date.plusDays(1)
                    .atStartOfDay(zoneId)
                    .toInstant();
            Instant segmentEnd = nextDay.isBefore(end) ? nextDay : end;

            if (businessCalendar.isBusinessDay(date)) {
                total = total.plus(Duration.between(cursor, segmentEnd));
            }

            cursor = segmentEnd;
        }

        return total;
    }

    private void updateBreachStatus() {
        breached = elapsedDuration.compareTo(allowedDuration) >= 0;
    }

    public boolean hasBreached() {
        return breached;
    }
}