package com.seatrush.ticket.infrastructure.config;

import com.seatrush.ticket.application.port.in.TicketCommandUseCase;
import com.seatrush.ticket.application.port.in.TicketQueryUseCase;
import com.seatrush.ticket.application.port.out.SeatReservationPort;
import com.seatrush.ticket.application.port.out.TicketQueryPort;
import com.seatrush.ticket.application.port.out.TicketRepository;
import com.seatrush.ticket.application.service.TicketCommandService;
import com.seatrush.ticket.application.service.TicketQueryService;
import com.seatrush.ticket.infrastructure.transaction.TransactionalTicketCommandUseCase;
import com.seatrush.ticket.infrastructure.transaction.TransactionalTicketQueryUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TicketModuleConfig {

    @Bean
    public TicketQueryUseCase ticketQueryUseCase(TicketQueryPort ticketQueryPort) {
        TicketQueryService service = new TicketQueryService(ticketQueryPort);
        return new TransactionalTicketQueryUseCase(service);
    }

    @Bean
    public TicketCommandUseCase ticketCommandUseCase(
            TicketRepository ticketRepository,
            SeatReservationPort seatReservationPort
    ) {
        TicketCommandService service = new TicketCommandService(ticketRepository, seatReservationPort);
        return new TransactionalTicketCommandUseCase(service);
    }
}
