package com.seatrush.ticket.infrastructure.web;

import com.seatrush.common.ErrorResponse;
import com.seatrush.ticket.domain.exception.InvalidTicketStateException;
import com.seatrush.ticket.domain.exception.TicketDomainException;
import com.seatrush.ticket.domain.exception.TicketNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = "com.seatrush.ticket.infrastructure.web")
public class TicketExceptionHandler {

    @ExceptionHandler(TicketNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(TicketNotFoundException exception) {
        return new ErrorResponse(
                "TICKET_NOT_FOUND",
                exception.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(InvalidTicketStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidState(InvalidTicketStateException exception) {
        return new ErrorResponse(
                "INVALID_TICKET_STATE",
                exception.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(RuntimeException exception) {
        return new ErrorResponse(
                "BAD_REQUEST",
                exception.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(TicketDomainException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDomainException(TicketDomainException exception) {
        return new ErrorResponse(
                "TICKET_DOMAIN_ERROR",
                exception.getMessage(),
                LocalDateTime.now()
        );
    }
}
