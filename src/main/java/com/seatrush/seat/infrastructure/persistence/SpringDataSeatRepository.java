package com.seatrush.seat.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataSeatRepository extends JpaRepository<SeatJpaEntity, UUID> {
    Optional<SeatJpaEntity> findByIdAndEventId(UUID seatId, UUID eventId);
    List<SeatJpaEntity> findByEventId(UUID eventId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatJpaEntity s WHERE s.id = :id")
    Optional<SeatJpaEntity> findByIdWithLock(@Param("id") UUID id);
}
