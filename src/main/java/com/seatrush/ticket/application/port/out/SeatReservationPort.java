package com.seatrush.ticket.application.port.out;

import java.util.UUID;

public interface SeatReservationPort {
    void reserve(UUID seatId);
    void release(UUID seatId);
    void sell(UUID seatId);
}
