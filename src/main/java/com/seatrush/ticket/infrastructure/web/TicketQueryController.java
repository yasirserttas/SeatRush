package com.seatrush.ticket.infrastructure.web;

import com.seatrush.ticket.application.port.in.TicketQueryUseCase;
import com.seatrush.ticket.application.query.TicketView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class TicketQueryController {

    private final TicketQueryUseCase ticketQueryUseCase;
    private final TicketWebMapper ticketWebMapper;

    @GetMapping("/tickets/{id}")
    public TicketResponse getById(
            @PathVariable UUID id
    ) {
        TicketView ticketView =
                ticketQueryUseCase.findById(id);

        return ticketWebMapper.toResponse(ticketView);
    }

    @GetMapping("/tickets/code/{ticketCode}")
    public TicketResponse getByTicketCode(
            @PathVariable String ticketCode
    ) {
        TicketView ticketView =
                ticketQueryUseCase.findByTicketCode(ticketCode);

        return ticketWebMapper.toResponse(ticketView);
    }

    @GetMapping("/users/{userId}/tickets")
    public TicketListResponse getByUserId(
            @PathVariable UUID userId
    ) {
        List<TicketView> ticketViews =
                ticketQueryUseCase.findByUserId(userId);

        List<TicketResponse> tickets =
                ticketWebMapper.toResponseList(ticketViews);

        return new TicketListResponse(
                tickets.size(),
                tickets
        );
    }
}
