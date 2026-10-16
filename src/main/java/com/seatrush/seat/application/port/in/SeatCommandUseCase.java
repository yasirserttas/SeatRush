package com.seatrush.seat.application.port.in;

import com.seatrush.seat.application.command.CreateSeatCommand;
import com.seatrush.seat.application.command.CreateSeatsForEventCommand;


import java.util.UUID;

public interface SeatCommandUseCase {
    UUID create(CreateSeatCommand command  );
    void createSeatsForEvent(CreateSeatsForEventCommand command);
    void release(UUID id);
    void reserve(UUID id);
    void sell(UUID id);
    void delete(UUID id);
}
