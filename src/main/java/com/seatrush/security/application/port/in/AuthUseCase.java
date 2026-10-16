package com.seatrush.security.application.port.in;

import com.seatrush.security.application.command.LoginCommand;
import com.seatrush.security.application.command.RegisterCommand;

import java.util.UUID;

public interface AuthUseCase {
    UUID register(RegisterCommand command);
    AuthTokenResponse login(LoginCommand command);
    AuthTokenResponse refresh(String refreshToken);
}