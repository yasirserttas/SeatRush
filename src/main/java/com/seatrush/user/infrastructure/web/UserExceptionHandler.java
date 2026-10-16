package com.seatrush.user.infrastructure.web;

import com.seatrush.common.ErrorResponse;
import com.seatrush.user.domain.exception.InvalidUserStateException;
import com.seatrush.user.domain.exception.UserDomainException;
import com.seatrush.user.domain.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = "com.seatrush.user.infrastructure.web")
public class UserExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(UserNotFoundException exception) {
        return new ErrorResponse(
                "USER_NOT_FOUND",
                exception.getMessage(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(InvalidUserStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidState(InvalidUserStateException exception) {
        return new ErrorResponse(
                "INVALID_USER_STATE",
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

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(org.springframework.web.bind.MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");
        return new ErrorResponse(
                "VALIDATION_ERROR",
                message,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(UserDomainException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDomainException(UserDomainException exception) {
        return new ErrorResponse(
                "USER_DOMAIN_ERROR",
                exception.getMessage(),
                LocalDateTime.now()
        );
    }
}
