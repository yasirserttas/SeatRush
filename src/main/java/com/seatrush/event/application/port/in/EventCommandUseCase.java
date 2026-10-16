package com.seatrush.event.application.port.in;

import com.seatrush.event.application.command.CreateEventCommand;
import com.seatrush.event.application.command.UpdateEventCommand;

import java.util.UUID;

public interface EventCommandUseCase {
    UUID create(CreateEventCommand command);
    void update(UpdateEventCommand command);
    void delete(UUID id);
    void activate(UUID eventId);
    void cancel(UUID eventId);
}
