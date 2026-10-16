package com.seatrush.event.application.query;

import java.time.LocalDateTime;

public record EventView(String title, String description, LocalDateTime startTime,
                        LocalDateTime endTime, Integer totalCapacity) {
}
