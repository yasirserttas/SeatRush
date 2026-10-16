package com.seatrush.security.application.service;

import com.seatrush.security.AuthUserPort;
import com.seatrush.security.AuthUserView;
import com.seatrush.security.application.command.LoginCommand;
import com.seatrush.security.application.command.RegisterCommand;
import com.seatrush.security.application.port.in.AuthTokenResponse;
import com.seatrush.security.application.port.in.AuthUseCase;
import com.seatrush.security.infrastructure.jwt.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

public class AuthService implements AuthUseCase {

    private final AuthUserPort authUserPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;


    public AuthService(AuthUserPort authUserPort,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider) {
        this.authUserPort = authUserPort;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }


    @Override
    public UUID register(RegisterCommand command) {
        return authUserPort.createUser(
                command.username(),
                command.email(),
                command.password(),
                command.firstName(),
                command.lastName());

    }

    @Override
    public AuthTokenResponse login(LoginCommand command) {

        AuthUserView user  = authUserPort.findByEmail(command.email())
                .orElseThrow(()-> new IllegalArgumentException("Invalid email or password."));

        if(!passwordEncoder.matches(command.password(), user.password())){
            throw new IllegalArgumentException("Invalid email or password.");
        }

        String accessToken = jwtProvider.generateAccessToken(command.email(), user.role());
        String refreshToken = jwtProvider.generateRefreshToken(command.email(), user.role());

        return new AuthTokenResponse(accessToken,refreshToken);

    }

    @Override
    public AuthTokenResponse refresh(String refreshToken) {

        if (!jwtProvider.validateToken(refreshToken) ||
                !"REFRESH".equals(jwtProvider.getTypeFromToken(refreshToken))) {

            throw new IllegalArgumentException("Invalid or expired refresh token.");
        }

        String email = jwtProvider.getEmailFromToken(refreshToken);

        AuthUserView user = authUserPort.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        String newAccessToken = jwtProvider.generateAccessToken(user.email(), user.role());

        return new AuthTokenResponse(newAccessToken,refreshToken);

    }
}
