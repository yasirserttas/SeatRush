package com.seatrush.event.application.command;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateEventCommand(UUID id, String title, String description, LocalDateTime startTime,
                                 LocalDateTime endTime, Integer totalCapacity) {
}
