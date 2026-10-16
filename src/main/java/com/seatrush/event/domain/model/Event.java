package com.seatrush.event.domain.model;

import com.seatrush.event.domain.exception.InvalidEventStateException;

import java.time.LocalDateTime;
import java.util.UUID;

public class Event {

    private final UUID id;
    private String title;
    private String description;
    private EventStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalCapacity;

    private Event(
            UUID id,
            String title,
            String description,
            EventStatus status,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int totalCapacity
    ) {
        validate(id, title, description, status, startTime, endTime, totalCapacity);
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalCapacity = totalCapacity;
    }

    public static Event create(
            String title,
            String description,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int totalCapacity
    ) {
        return new Event(
                UUID.randomUUID(),
                title,
                description,
                EventStatus.DRAFT,
                startTime,
                endTime,
                totalCapacity
        );
    }

    public static Event restore(
            UUID id,
            String title,
            String description,
            EventStatus status,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int totalCapacity
    ) {
        return new Event(
                id,
                title,
                description,
                status,
                startTime,
                endTime,
                totalCapacity
        );
    }

    public void updateDetails(
            String title,
            String description,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int totalCapacity
    ) {
        if (this.status == EventStatus.CANCELLED) {
            throw new InvalidEventStateException("Cancelled event cannot be updated.");
        }

        validate(id, title, description, status, startTime, endTime, totalCapacity);

        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalCapacity = totalCapacity;
    }

    public void activate() {
        if (status != EventStatus.DRAFT) {
            throw new InvalidEventStateException(
                    "Only draft event can be activated."
            );
        }

        this.status = EventStatus.ACTIVE;
    }

    public void cancel() {
        if (status == EventStatus.CANCELLED) {
            throw new InvalidEventStateException(
                    "Event is already cancelled."
            );
        }

        this.status = EventStatus.CANCELLED;
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Event ID cannot be null."
            );
        }
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Event title cannot be blank."
            );
        }
    }

    private static void validateDescription(String description){
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Event description cannot be blank.");
        }
    }

    private static void validateTimes(
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException(
                    "Start and end times cannot be null."
            );
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    "End time must be after start time."
            );
        }
    }

    private static void validateStatus(EventStatus eventStatus){
        if (eventStatus == null) {
            throw new IllegalArgumentException(
                    "Event status cannot be null."
            );
        }
    }

    private static void validateCapacity(int totalCapacity) {
        if ( totalCapacity <= 0) {
            throw new IllegalArgumentException(
                    "Total capacity must be greater than zero."
            );
        }
    }

    private static void validate(UUID id , String title, String description, EventStatus status,
                                 LocalDateTime startTime, LocalDateTime
                                         endTime, int totalCapacity) {
        validateId(id);
        validateTitle(title);
        validateDescription(description);
        validateStatus(status);
        validateTimes(startTime, endTime);
        validateCapacity(totalCapacity);
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public EventStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }
}