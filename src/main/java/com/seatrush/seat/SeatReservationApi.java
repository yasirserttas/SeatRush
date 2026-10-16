package com.seatrush.seat;

import java.util.UUID;

public interface SeatReservationApi {
    void reserve(UUID seatId);
    void release(UUID seatId);
    void sell(UUID seatId);
}
