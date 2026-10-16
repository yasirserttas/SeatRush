package com.seatrush.user.application.command;

import com.seatrush.user.domain.model.UserRole;

public record CreateUserCommand(
        String username,
        String email,
        String password,
        String firstName,
        String lastName,
        UserRole role
) {
}
