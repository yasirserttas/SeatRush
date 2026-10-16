package com.seatrush.security;

import java.util.UUID;

public record AuthUserView(
        UUID id,
        String email,
        String password,
        String role,
        String status
) {
}
