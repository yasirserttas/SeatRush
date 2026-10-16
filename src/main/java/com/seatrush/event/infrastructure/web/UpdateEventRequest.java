package com.seatrush.event.infrastructure.web;

import java.time.LocalDateTime;

public record UpdateEventRequest(
        String title,
        String description,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer totalCapacity
) {
}