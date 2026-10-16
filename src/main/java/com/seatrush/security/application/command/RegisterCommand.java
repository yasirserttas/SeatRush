package com.seatrush.security.application.command;

public record RegisterCommand(
        String username,
        String email,
        String password,
        String firstName,
        String lastName
) {
}