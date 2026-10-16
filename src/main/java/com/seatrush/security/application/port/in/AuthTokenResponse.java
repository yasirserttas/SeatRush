package com.seatrush.security.application.port.in;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken
) {
}