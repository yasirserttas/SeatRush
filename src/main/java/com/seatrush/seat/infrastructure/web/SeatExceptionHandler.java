package com.seatrush.seat.infrastructure.web;

import com.seatrush.common.ErrorResponse;
import com.seatrush.seat.domain.exception.InvalidSeatStateException;
import com.seatrush.seat.domain.exception.SeatDomainException;
import com.seatrush.seat.domain.exception.SeatNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = "com.seatrush.seat.infrastructure.web")
public class SeatExceptionHandler {

    @ExceptionHandler(SeatNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(SeatNotFoundException exception) {
        return new ErrorResponse(
                "SEAT_NOT_FOUND",
                exception.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(InvalidSeatStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidState(InvalidSeatStateException exception) {
        return new ErrorResponse(
                "INVALID_SEAT_STATE",
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

    @ExceptionHandler(SeatDomainException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDomainException(SeatDomainException exception) {
        return new ErrorResponse(
                "SEAT_DOMAIN_ERROR",
                exception.getMessage(),
                LocalDateTime.now()
        );
    }
}
