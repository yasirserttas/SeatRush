package com.seatrush.event.application.command;

import java.time.LocalDateTime;

public record CreateEventCommand(String title, String description, LocalDateTime startTime,
                                 LocalDateTime endTime, Integer totalCapacity) {
}
