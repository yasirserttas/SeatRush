package com.seatrush.event.application.service;

import com.seatrush.event.application.command.CreateEventCommand;
import com.seatrush.event.application.command.UpdateEventCommand;
import com.seatrush.event.application.port.in.EventCommandUseCase;
import com.seatrush.event.application.port.out.EventPublisher;
import com.seatrush.event.application.port.out.EventRepository;
import com.seatrush.event.EventCreated;
import com.seatrush.event.domain.exception.EventNotFoundException;
import com.seatrush.event.domain.model.Event;

import java.util.UUID;

public class EventCommandService implements EventCommandUseCase {

    private final EventRepository eventRepository;
    private final EventPublisher eventPublisher;

    public EventCommandService(EventRepository eventRepository, EventPublisher eventPublisher) {
        this.eventRepository = eventRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UUID create(CreateEventCommand command) {
        Event event = Event.create(
                command.title(),
                command.description(),
                command.startTime(),
                command.endTime(),
                command.totalCapacity()
        );

        Event savedEvent = eventRepository.save(event);
        eventPublisher.publish(new EventCreated(savedEvent.getId(), savedEvent.getTotalCapacity()));

        return savedEvent.getId();
    }

    @Override
    public void update(UpdateEventCommand command) {
        Event event = eventRepository.findById(command.id())
                .orElseThrow(() -> new EventNotFoundException(command.id()));

        event.updateDetails(
                command.title(),
                command.description(),
                command.startTime(),
                command.endTime(),
                command.totalCapacity()
        );

        eventRepository.save(event);
    }

    @Override
    public void delete(UUID id) {
        Event deleteEvent = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        eventRepository.delete(deleteEvent.getId());
    }

    @Override
    public void activate(UUID eventId) {
        Event activeEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        activeEvent.activate();
        eventRepository.save(activeEvent);
    }

    @Override
    public void cancel(UUID eventId) {
        Event cancelEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        cancelEvent.cancel();
        eventRepository.save(cancelEvent);
    }
}
