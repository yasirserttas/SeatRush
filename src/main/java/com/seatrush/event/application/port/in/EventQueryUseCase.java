package com.seatrush.event.application.port.in;

import com.seatrush.event.application.query.EventView;

import java.util.List;
import java.util.UUID;

public interface EventQueryUseCase {
    EventView getById(UUID id);
    EventView getByTitle(String title);
    List<EventView> getAll();
}
