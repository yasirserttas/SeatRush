package com.seatrush.ticket.infrastructure.web;

import com.seatrush.ticket.application.command.CreateTicketCommand;
import com.seatrush.ticket.application.port.in.TicketCommandUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketCommandController {

    private final TicketCommandUseCase ticketCommandUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTicketResponse create(
            @Valid @RequestBody CreateTicketRequest request
    ) {
        UUID ticketId = ticketCommandUseCase.create(
                new CreateTicketCommand(
                        request.userId(),
                        request.seatId(),
                        request.eventId()
                )
        );

        return new CreateTicketResponse(ticketId);
    }

    @PatchMapping("/{id}/use")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void use(
            @PathVariable UUID id
    ) {
        ticketCommandUseCase.useTicket(id);
    }

    @PatchMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(
            @PathVariable UUID id
    ) {
        ticketCommandUseCase.cancelTicket(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id
    ) {
        ticketCommandUseCase.delete(id);
    }
}
