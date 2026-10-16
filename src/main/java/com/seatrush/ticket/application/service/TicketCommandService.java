package com.seatrush.ticket.application.service;

import com.seatrush.ticket.application.command.CreateTicketCommand;
import com.seatrush.ticket.application.port.in.TicketCommandUseCase;
import com.seatrush.ticket.application.port.out.SeatReservationPort;
import com.seatrush.ticket.application.port.out.TicketRepository;
import com.seatrush.ticket.domain.exception.TicketNotFoundException;
import com.seatrush.ticket.domain.model.Ticket;

import java.util.UUID;

public class TicketCommandService implements TicketCommandUseCase {

    private final TicketRepository ticketRepository;
    private final SeatReservationPort seatReservationPort;

    public TicketCommandService(TicketRepository ticketRepository, SeatReservationPort seatReservationPort) {
        this.ticketRepository = ticketRepository;
        this.seatReservationPort = seatReservationPort;
    }

    @Override
    public UUID create(CreateTicketCommand command) {
        Ticket ticket = Ticket.create(command.userId(), command.eventId(), command.seatId());
        seatReservationPort.sell(command.seatId());
        Ticket savedTicket = ticketRepository.save(ticket);
        return savedTicket.getId();
    }

    @Override
    public void delete(UUID id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
        if (ticket.holdsSeat()) {
            seatReservationPort.release(ticket.getSeatId());
        }
        ticketRepository.delete(ticket.getId());
    }

    @Override
    public void useTicket(UUID id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
        ticket.use();
        ticketRepository.save(ticket);
    }

    @Override
    public void cancelTicket(UUID id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
        ticket.cancel();
        seatReservationPort.release(ticket.getSeatId());
        ticketRepository.save(ticket);
    }
}
