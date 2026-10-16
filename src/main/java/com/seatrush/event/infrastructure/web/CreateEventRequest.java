package com.seatrush.event.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record CreateEventRequest(
        @NotBlank
        String title,
        @NotBlank
        String description,
        @NotNull
        LocalDateTime startTime,
        @NotNull
        LocalDateTime endTime,
        @NotNull
        @Positive
        Integer totalCapacity
) {
}
