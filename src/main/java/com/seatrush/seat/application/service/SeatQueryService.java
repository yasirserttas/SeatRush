package com.seatrush.seat.application.service;


import com.seatrush.seat.application.port.in.SeatQueryUseCase;
import com.seatrush.seat.application.port.out.SeatQueryPort;
import com.seatrush.seat.application.query.SeatView;
import com.seatrush.seat.domain.exception.SeatNotFoundException;

import java.util.List;
import java.util.UUID;

public class SeatQueryService implements SeatQueryUseCase {

    private final SeatQueryPort seatQueryPort;
    public SeatQueryService(SeatQueryPort seatQueryPort) {
        this.seatQueryPort = seatQueryPort;
    }

    @Override
    public SeatView getById(UUID id) {

        return seatQueryPort.findViewBySeatId(id)
                .orElseThrow(() -> new SeatNotFoundException(id));
    }

    @Override
    public SeatView getByIdAndEventId(UUID id, UUID eventId) {
        return seatQueryPort.findViewByIdAndEventId(id, eventId)
                .orElseThrow(() -> new SeatNotFoundException(id, eventId));
    }

    @Override
    public List<SeatView> getByEventId(UUID id) {
        return seatQueryPort.findByEventId(id);
    }

    @Override
    public List<SeatView> getAll() {
        return seatQueryPort.findAll();
    }
}
