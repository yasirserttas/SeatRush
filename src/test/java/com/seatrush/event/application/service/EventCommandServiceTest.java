package com.seatrush.event.application.service;

import com.seatrush.event.application.command.CreateEventCommand;
import com.seatrush.event.application.command.UpdateEventCommand;
import com.seatrush.event.application.port.out.EventPublisher;
import com.seatrush.event.application.port.out.EventRepository;
import com.seatrush.event.EventCreated;
import com.seatrush.event.domain.exception.EventNotFoundException;
import com.seatrush.event.domain.exception.InvalidEventStateException;
import com.seatrush.event.domain.model.Event;
import com.seatrush.event.domain.model.EventStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventCommandService Unit Tests")
public class EventCommandServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private EventCommandService eventCommandService;

    private static final String title = "title";
    private static final String desc = "desc";
    private static final LocalDateTime startTime = LocalDateTime.of(2026, 11, 15, 23, 23);
    private static final LocalDateTime endTime = LocalDateTime.of(2026, 11, 17, 23, 23);
    private static final int totalCapacity = 15;

    @Nested
    @DisplayName("Create Tests")
    class Create {

        @Test
        @DisplayName("should create, save event, publish event and return event id when command is valid")
        void shouldCreateSaveEventAndReturnEventIdWhenCommandIsValid() {
            CreateEventCommand command = new CreateEventCommand(title, desc, startTime, endTime, totalCapacity);

            Event repositorySavedEvent = Event.create(
                    command.title(),
                    command.description(),
                    command.startTime(),
                    command.endTime(),
                    command.totalCapacity()
            );

            when(eventRepository.save(any(Event.class))).thenReturn(repositorySavedEvent);

            ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);

            UUID actualEventId = eventCommandService.create(command);

            verify(eventRepository).save(eventArgumentCaptor.capture());

            Event captureEvent = eventArgumentCaptor.getValue();

            verify(eventPublisher).publish(
                    new EventCreated(repositorySavedEvent.getId(), repositorySavedEvent.getTotalCapacity())
            );

            assertThat(actualEventId).isEqualTo(repositorySavedEvent.getId());
            assertThat(captureEvent.getTitle()).isEqualTo(title);
            assertThat(captureEvent.getDescription()).isEqualTo(desc);
            assertThat(captureEvent.getStartTime()).isEqualTo(startTime);
            assertThat(captureEvent.getEndTime()).isEqualTo(endTime);
            assertThat(captureEvent.getTotalCapacity()).isEqualTo(totalCapacity);
        }

        @Test
        @DisplayName("should not publish EventCreated when save fails")
        void shouldNotPublishEventCreatedWhenSaveFails() {
            CreateEventCommand command = new CreateEventCommand(title, desc, startTime, endTime, totalCapacity);

            when(eventRepository.save(any(Event.class)))
                    .thenThrow(new RuntimeException("db unavailable"));

            assertThatThrownBy(() -> eventCommandService.create(command))
                    .isInstanceOf(RuntimeException.class);

            verify(eventPublisher, never()).publish(any());
        }
    }

    @Nested
    @DisplayName("Update Tests")
    class Update {

        @Test
        @DisplayName("should update event and save when command is valid")
        void shouldUpdateEventAndSaveWhenCommandIsValid() {
            Event event = Event.create(title, desc, startTime, endTime, totalCapacity);
            UUID testEventId = event.getId();

            UpdateEventCommand updateEventCommand = new UpdateEventCommand(
                    testEventId,
                    "title123",
                    "updatedDesc",
                    startTime.plusDays(1),
                    endTime.plusDays(3),
                    444
            );

            when(eventRepository.findById(testEventId)).thenReturn(Optional.of(event));

            eventCommandService.update(updateEventCommand);

            verify(eventRepository).findById(testEventId);
            verify(eventRepository).save(event);

            assertThat(event.getId()).isEqualTo(testEventId);
            assertThat(event.getTitle()).isEqualTo("title123");
            assertThat(event.getDescription()).isEqualTo("updatedDesc");
            assertThat(event.getStartTime()).isEqualTo(startTime.plusDays(1));
            assertThat(event.getEndTime()).isEqualTo(endTime.plusDays(3));
            assertThat(event.getTotalCapacity()).isEqualTo(444);
        }

        @Test
        @DisplayName("should throw EventNotFoundException when updated event is not found by id")
        void shouldThrowEventNotFoundExceptionWhenUpdatedEventIsNotFoundId() {
            UUID testEventId = UUID.randomUUID();

            UpdateEventCommand updateEventCommand = new UpdateEventCommand(
                    testEventId,
                    "title123",
                    "updatedDesc",
                    startTime.plusDays(1),
                    endTime.plusDays(3),
                    444
            );

            when(eventRepository.findById(testEventId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventCommandService.update(updateEventCommand))
                    .isInstanceOf(EventNotFoundException.class);

            verify(eventRepository).findById(testEventId);
            verify(eventRepository, never()).save(any(Event.class));
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class Delete {

        @Test
        @DisplayName("should delete event when event is found by id")
        void shouldDeleteEventWhenEventIsFoundById() {
            Event event = Event.create(title, desc, startTime, endTime, totalCapacity);

            when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

            eventCommandService.delete(event.getId());

            verify(eventRepository).findById(event.getId());
            verify(eventRepository).delete(event.getId());
        }

        @Test
        @DisplayName("should throw EventNotFoundException when delete event is not found by id")
        void shouldThrowEventNotFoundExceptionWhenDeleteEventIsNotFoundById() {
            UUID testId = UUID.randomUUID();

            when(eventRepository.findById(testId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventCommandService.delete(testId))
                    .isInstanceOf(EventNotFoundException.class);

            verify(eventRepository).findById(testId);
            verify(eventRepository, never()).delete(any(UUID.class));
        }
    }

    @Nested
    @DisplayName("Activate Tests")
    class Activate {

        @Test
        @DisplayName("should activate event and save when event is found by id")
        void shouldActivateEventAndSaveWhenEventIsFoundById() {
            Event event = Event.create(title, desc, startTime, endTime, totalCapacity);
            UUID eventTest = event.getId();

            when(eventRepository.findById(eventTest)).thenReturn(Optional.of(event));

            eventCommandService.activate(eventTest);

            assertThat(event.getStatus()).isEqualTo(EventStatus.ACTIVE);

            verify(eventRepository).findById(eventTest);
            verify(eventRepository).save(any(Event.class));
        }

        @Test
        @DisplayName("should throw EventNotFoundException when event is not found by id")
        void shouldThrowEventNotFoundExceptionWhenEventIsNotFoundById() {
            UUID testId = UUID.randomUUID();

            when(eventRepository.findById(testId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventCommandService.activate(testId))
                    .isInstanceOf(EventNotFoundException.class);

            verify(eventRepository).findById(testId);
            verify(eventRepository, never()).save(any(Event.class));
        }

        @Test
        @DisplayName("should throw InvalidEventStateException and not save event when activation fails")
        void shouldThrowInvalidEventStateExceptionNotSaveEventWhenActivationFails() {
            Event event = Event.create(title, desc, startTime, endTime, totalCapacity);
            UUID test = event.getId();

            event.activate();

            when(eventRepository.findById(test)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventCommandService.activate(test))
                    .isInstanceOf(InvalidEventStateException.class);

            verify(eventRepository).findById(test);
            verify(eventRepository, never()).save(any(Event.class));
        }
    }

    @Nested
    @DisplayName("Cancel Tests")
    class Cancel {

        @Test
        @DisplayName("should cancel event and save when event is found by id")
        void shouldCancelledEventAndSaveWhenEventIdIsFoundById() {
            Event event = Event.create(title, desc, startTime, endTime, totalCapacity);
            UUID test = event.getId();

            when(eventRepository.findById(test)).thenReturn(Optional.of(event));

            eventCommandService.cancel(test);

            assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);

            verify(eventRepository).findById(test);
            verify(eventRepository).save(event);
        }

        @Test
        @DisplayName("should throw EventNotFoundException when event is not found by id")
        void shouldThrowEventNotFoundExceptionWhenEventIsNotFoundById() {
            UUID test = UUID.randomUUID();

            when(eventRepository.findById(test)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventCommandService.cancel(test))
                    .isInstanceOf(EventNotFoundException.class);

            verify(eventRepository).findById(test);
            verify(eventRepository, never()).save(any(Event.class));
        }

        @Test
        @DisplayName("should throw InvalidEventStateException and not save when cancel fails")
        void shouldThrowInvalidEventStateExceptionAndNotSaveWhenCancelledFails() {
            Event event = Event.create(title, desc, startTime, endTime, totalCapacity);
            UUID test = event.getId();
            event.cancel();

            when(eventRepository.findById(test)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventCommandService.cancel(test))
                    .isInstanceOf(InvalidEventStateException.class);

            verify(eventRepository).findById(test);
            verify(eventRepository, never()).save(any(Event.class));
        }
    }
}
