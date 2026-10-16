package com.seatrush.event.infrastructure.web;

import com.seatrush.common.ErrorResponse;
import com.seatrush.event.domain.exception.EventDomainException;
import com.seatrush.event.domain.exception.EventNotFoundException;
import com.seatrush.event.domain.exception.InvalidEventStateException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = "com.seatrush.event.infrastructure.web")
public class EventExceptionHandler {

    @ExceptionHandler(EventNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(EventNotFoundException exception) {
        return new ErrorResponse(
                "EVENT_NOT_FOUND",
                exception.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(InvalidEventStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidState(InvalidEventStateException exception) {
        return new ErrorResponse(
                "INVALID_EVENT_STATE",
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

    @ExceptionHandler(EventDomainException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDomainException(EventDomainException exception) {
        return new ErrorResponse(
                "EVENT_DOMAIN_ERROR",
                exception.getMessage(),
                LocalDateTime.now()
        );
    }
}