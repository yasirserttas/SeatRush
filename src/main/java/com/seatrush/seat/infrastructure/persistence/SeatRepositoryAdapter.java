package com.seatrush.seat.infrastructure.persistence;

import com.seatrush.seat.application.port.out.SeatRepository;
import com.seatrush.seat.domain.model.Seat;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SeatRepositoryAdapter implements SeatRepository {

    private final SpringDataSeatRepository springDataSeatRepository;
    private final SeatMapper seatMapper;


    @Override
    public Seat save(Seat seat) {
        return seatMapper.toEntity(springDataSeatRepository.save(seatMapper.toJpaEntity(seat)));
    }

    @Override
    public void saveAll(List<Seat> seats) {

        List<SeatJpaEntity> jpaSeats = seats.stream().map(seatMapper::toJpaEntity).toList();
        springDataSeatRepository.saveAll(jpaSeats);
    }

    @Override
    public Optional<Seat> findById(UUID id) {
        return springDataSeatRepository.findById(id).map(seatMapper::toEntity);
    }

    @Override
    public Optional<Seat> findByIdAndEventId(UUID id, UUID eventId) {
        return springDataSeatRepository.findByIdAndEventId(id, eventId).map(seatMapper::toEntity);
    }

    @Override
    public List<Seat> findByEventId(UUID eventId) {
        return springDataSeatRepository.findByEventId(eventId).stream().
                map(seatMapper::toEntity).toList();
    }

    @Override
    public void delete(UUID id) {
        springDataSeatRepository.deleteById(id);
    }

    @Override
    public Optional<Seat> findByIdWithLock(UUID id) {

        return springDataSeatRepository.findByIdWithLock(id).map(seatMapper::toEntity);

    }
}
