package com.seatrush.security.infrastructure.web;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank()
        String refreshToken
) {
}