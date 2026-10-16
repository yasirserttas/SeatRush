package com.seatrush.event.domain.exception;

import java.util.UUID;

public class EventNotFoundException extends EventDomainException {

    public EventNotFoundException(UUID id) {
        super("Event not found with id: " + id);
    }

    public EventNotFoundException(String title) {
        super("Event not found with title: " + title);
    }
}
