package com.seatrush.ticket.infrastructure.persistence;

import com.seatrush.ticket.application.port.out.TicketQueryPort;
import com.seatrush.ticket.application.query.TicketView;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TicketQueryAdapter implements TicketQueryPort {

    private final SpringDataTicketRepository  springDataTicketRepository;
    private final TicketMapper ticketMapper;

    @Override
    public Optional<TicketView> findById(UUID id) {
        return springDataTicketRepository.findById(id).map(ticketMapper::toView);
    }

    @Override
    public Optional<TicketView> findByTicketCode(String ticketCode) {
        return springDataTicketRepository.findByTicketCode(ticketCode).map(ticketMapper::toView);
    }

    @Override
    public List<TicketView> findByUserId(UUID userId) {
        return springDataTicketRepository.findByUserId(userId).stream().map(ticketMapper::toView).toList();
    }
}
