package com.seatrush.event.application.port.out;

import com.seatrush.event.application.query.EventView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventQueryPort {
    Optional<EventView> findById(UUID id);
    Optional<EventView> findByTitle(String title);
    List<EventView> findAll();
}
