package com.seatrush.security.application.command;

public record LoginCommand(
        String email,
        String password
) {
}