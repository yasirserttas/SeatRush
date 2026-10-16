package com.seatrush.seat.infrastructure.messaging;

import com.seatrush.event.EventCreated;
import com.seatrush.seat.application.command.CreateSeatsForEventCommand;
import com.seatrush.seat.application.port.in.SeatCommandUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("EventCreatedListener Unit Tests")
class EventCreatedListenerTest {

    private final SeatCommandUseCase seatCommandUseCase = mock(SeatCommandUseCase.class);
    private final EventCreatedListener listener = new EventCreatedListener(seatCommandUseCase);

    @Test
    @DisplayName("should create seats with the event's id and capacity when EventCreated is received")
    void shouldCreateSeatsWithEventIdAndCapacity() {
        UUID eventId = UUID.randomUUID();
        int capacity = 250;
        ArgumentCaptor<CreateSeatsForEventCommand> captor =
                ArgumentCaptor.forClass(CreateSeatsForEventCommand.class);

        listener.onEventCreated(new EventCreated(eventId, capacity));

        verify(seatCommandUseCase).createSeatsForEvent(captor.capture());

        CreateSeatsForEventCommand command = captor.getValue();
        assertThat(command.eventId()).isEqualTo(eventId);
        assertThat(command.capacity()).isEqualTo(capacity);
    }

    @Test
    @DisplayName("should propagate failures from seat creation")
    void shouldPropagateSeatCreationFailures() {
        EventCreated event = new EventCreated(UUID.randomUUID(), 10);
        doThrow(new RuntimeException("seat creation failed"))
                .when(seatCommandUseCase).createSeatsForEvent(any());

        assertThatThrownBy(() -> listener.onEventCreated(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("seat creation failed");
    }
}
