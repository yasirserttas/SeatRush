package com.seatrush.event.application.port.out;

import com.seatrush.event.domain.model.Event;

import java.util.Optional;
import java.util.UUID;


public interface EventRepository {
    Event save(Event event);
    Optional<Event> findById(UUID id);
    void delete(UUID id);
}
