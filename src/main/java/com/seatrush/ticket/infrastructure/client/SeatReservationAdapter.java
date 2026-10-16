package com.seatrush.ticket.infrastructure.client;

import com.seatrush.seat.SeatReservationApi;
import com.seatrush.ticket.application.port.out.SeatReservationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SeatReservationAdapter implements SeatReservationPort {

    private final SeatReservationApi seatReservationApi;

    @Override
    public void reserve(UUID seatId) {
        seatReservationApi.reserve(seatId);
    }

    @Override
    public void release(UUID seatId) {
        seatReservationApi.release(seatId);
    }

    @Override
    public void sell(UUID seatId) {
        seatReservationApi.sell(seatId);
    }
}
