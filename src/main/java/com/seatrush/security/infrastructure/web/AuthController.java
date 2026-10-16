package com.seatrush.security.infrastructure.web;

import com.seatrush.security.application.command.LoginCommand;
import com.seatrush.security.application.command.RegisterCommand;
import com.seatrush.security.application.port.in.AuthTokenResponse;
import com.seatrush.security.application.port.in.AuthUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthUseCase authUseCase;


    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UUID register(@Valid @RequestBody RegisterRequest request) {
        return authUseCase.register(new RegisterCommand(
                request.username(),
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName()
        ));
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        AuthTokenResponse tokenResponse = authUseCase.login(new LoginCommand(
                request.email(),
                request.password()
        ));
        return new AuthResponse(
                tokenResponse.accessToken(),
                tokenResponse.refreshToken()
        );
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthTokenResponse tokenResponse = authUseCase.refresh(request.refreshToken());
        return new AuthResponse(
                tokenResponse.accessToken(),
                tokenResponse.refreshToken()
        );
    }

}
