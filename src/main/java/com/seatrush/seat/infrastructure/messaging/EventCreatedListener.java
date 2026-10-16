package com.seatrush.seat.infrastructure.messaging;


import com.seatrush.event.EventCreated;
import com.seatrush.seat.application.command.CreateSeatsForEventCommand;
import com.seatrush.seat.application.port.in.SeatCommandUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventCreatedListener {

    private final SeatCommandUseCase seatCommandUseCase;

    @ApplicationModuleListener
    public void onEventCreated(EventCreated event) {
        seatCommandUseCase.createSeatsForEvent(
                new CreateSeatsForEventCommand(
                        event.eventId(),
                        event.totalCapacity()
                )
        );
    }
}
