package com.seatrush.ticket.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataTicketRepository extends JpaRepository<TicketJpaEntity, UUID> {
    Optional<TicketJpaEntity> findByTicketCode(String ticketCode);

    List<TicketJpaEntity> findByUserId(UUID userId);
}
