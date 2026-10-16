package com.seatrush.seat.infrastructure.persistence;

import com.seatrush.seat.application.port.out.SeatQueryPort;
import com.seatrush.seat.application.query.SeatView;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SeatQueryAdapter implements SeatQueryPort {

    private final SpringDataSeatRepository springDataSeatRepository;
    private final SeatMapper seatMapper;


    @Override
    public Optional<SeatView> findViewBySeatId(UUID seatId) {
        return springDataSeatRepository.findById(seatId).map(seatMapper::toView);
    }

    @Override
    public Optional<SeatView> findViewByIdAndEventId(UUID seatId, UUID eventId) {
        return springDataSeatRepository.findByIdAndEventId(seatId, eventId).map(seatMapper::toView);
    }

    @Override
    public List<SeatView> findByEventId(UUID eventId) {
        return springDataSeatRepository.findByEventId(eventId).stream().map(seatMapper::toView).toList();
    }

    @Override
    public List<SeatView> findAll() {
        return springDataSeatRepository.findAll().stream().map(seatMapper::toView).toList();
    }
}
