package com.seatrush.user.application.query;

import com.seatrush.user.domain.model.UserRole;
import com.seatrush.user.domain.model.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserView(
        UUID id,
        String username,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        UserStatus status,
        LocalDateTime createdAt
) {
}
