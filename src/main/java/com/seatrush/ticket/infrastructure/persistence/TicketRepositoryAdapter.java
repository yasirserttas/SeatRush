package com.seatrush.ticket.infrastructure.persistence;

import com.seatrush.ticket.application.port.out.TicketRepository;
import com.seatrush.ticket.domain.model.Ticket;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TicketRepositoryAdapter implements TicketRepository {

    private final TicketMapper ticketMapper;
    private final SpringDataTicketRepository  jpaRepository;

    @Override
    public Ticket save(Ticket ticket) {
        return ticketMapper.toEntity(jpaRepository.save(ticketMapper.toJpaEntity(ticket)));
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<Ticket> findById(UUID id) {
        return jpaRepository.findById(id).map(ticketMapper::toEntity);
    }

    @Override
    public Optional<Ticket> findByTicketCode(String ticketCode) {
        return jpaRepository.findByTicketCode(ticketCode).map(ticketMapper::toEntity);
    }
}
