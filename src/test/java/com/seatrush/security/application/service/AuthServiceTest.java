package com.seatrush.security.application.service;

import com.seatrush.security.AuthUserPort;
import com.seatrush.security.AuthUserView;
import com.seatrush.security.application.command.LoginCommand;
import com.seatrush.security.application.command.RegisterCommand;
import com.seatrush.security.application.port.in.AuthTokenResponse;
import com.seatrush.security.infrastructure.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private  AuthUserPort authUserPort;

    @Mock
    private  PasswordEncoder passwordEncoder;

    @Mock
    private  JwtProvider jwtProvider;

    @InjectMocks
    private  AuthService authService;



    @Nested
    class Register{

        @Test
        @DisplayName("should create user and return id when command is valid")
        void shouldCreateUserAndReturnIdWhenCommandIsValid(){

            RegisterCommand registerCommand = new RegisterCommand
                    ("yasirserttas",
                            "yasirserttas@gmail.com",
                            "yasir2544-",
                            "yasir",
                            "serttas");

            UUID exceptedUserId = UUID.randomUUID();

            when(authUserPort.createUser(
                    registerCommand.username(),
                    registerCommand.email(),
                    registerCommand.password(),
                    registerCommand.firstName(),
                    registerCommand.lastName())).thenReturn(exceptedUserId);

            UUID actualUserId = authService.register(registerCommand);

            assertThat(actualUserId).isEqualTo(exceptedUserId);

            verify(authUserPort).createUser(
                    registerCommand.username(),
                    registerCommand.email(),
                    registerCommand.password(),
                    registerCommand.firstName(),
                    registerCommand.lastName());

        }

    }

    @Nested
    class Login{

        @Test
        @DisplayName("should login user and return auth token responses when credentials are valid")
        void shouldLoginUserAndReturnAuthTokenResponsesWhenCredentialAreValid(){

            LoginCommand loginCommand = new LoginCommand("yasirrserttas@gmail.com","password12344-");

            AuthUserView authUserView = new AuthUserView(
                    UUID.randomUUID(),
                    loginCommand.email(),
                    loginCommand.password(),
                    "ADMIN",
                    "ACTIVE"
            );

            when(authUserPort.findByEmail(loginCommand.email())).thenReturn(Optional.of(authUserView));

            when(passwordEncoder.matches(loginCommand.password(),authUserView.password())).thenReturn(true);

            when(jwtProvider.generateRefreshToken(authUserView.email()
                    ,authUserView.role())).thenReturn("refreshToken");

            when(jwtProvider.generateAccessToken(authUserView.email()
                    ,authUserView.role())).thenReturn("accessToken");


            AuthTokenResponse actualAuthTokenResponse = authService.login(loginCommand);

            assertThat(actualAuthTokenResponse).isNotNull();
            assertThat(actualAuthTokenResponse.accessToken()).isEqualTo("accessToken");
            assertThat(actualAuthTokenResponse.refreshToken()).isEqualTo("refreshToken");

            verify(authUserPort).findByEmail(loginCommand.email());
            verify(passwordEncoder).matches(loginCommand.password(),authUserView.password());
            verify(jwtProvider).generateAccessToken(authUserView.email(),authUserView.role());
            verify(jwtProvider).generateRefreshToken(authUserView.email(),authUserView.role());


        }


        @Test
        @DisplayName("should throw IllegalArgumentException when user is not found by email")
        void shouldThrowIllegalArgumentExceptionWhenUserIsNotFoundByEmail(){

            LoginCommand loginCommand = new LoginCommand("yasirrserttas@gmail.com","password12344-");

            when(authUserPort.findByEmail(loginCommand.email())).thenReturn(Optional.empty());

            assertThatThrownBy(()-> authService.login(loginCommand))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(authUserPort).findByEmail(loginCommand.email());
            verify(passwordEncoder,never()).matches(anyString(),anyString());
            verify(jwtProvider,never()).generateRefreshToken(anyString(),anyString());
            verify(jwtProvider,never()).generateAccessToken(anyString(),anyString());

        }

        @Test
        @DisplayName("should throw IllegalArgumentException when password does not match")
        void shouldThrowIllegalArgumentExceptionWhenPasswordDoesNotMatch(){

            LoginCommand loginCommand = new LoginCommand("yasirrserttas@gmail.com","password12344-");

            AuthUserView authUserView = new AuthUserView(
                    UUID.randomUUID(),
                    loginCommand.email(),
                    "2344yasir125",
                    "ADMIN",
                    "ACTIVE"
            );

            when(authUserPort.findByEmail(loginCommand.email())).thenReturn(Optional.of(authUserView));
            when(passwordEncoder.matches(loginCommand.password(),authUserView.password())).thenReturn(false);

            assertThatThrownBy(()-> authService.login(loginCommand)).isInstanceOf(IllegalArgumentException.class);

            verify(authUserPort).findByEmail(loginCommand.email());
            verify(passwordEncoder).matches(loginCommand.password(),authUserView.password());
            verify(jwtProvider,never()).generateRefreshToken(anyString(),anyString());
            verify(jwtProvider,never()).generateAccessToken(anyString(),anyString());
        }

    }

    @Nested
    class Refresh{


        @Test
        @DisplayName("should refresh and return access token when refresh token is valid")
        void shouldRefreshAndReturnsAccessTokenWhenRefreshTokenIsValid(){

            String refreshToken = "sfuıfgaehfuıewhfıuewhhkuhaueıfe";

            AuthUserView user = new AuthUserView(
                    UUID.randomUUID(),
                    "yasir@mail.com",
                    "ajkahfgaeuyfaeh",
                    "ADMIN",
                    "ACTIVE");

            when(jwtProvider.validateToken(refreshToken)).thenReturn(true);
            when(jwtProvider.getTypeFromToken(refreshToken)).thenReturn("REFRESH");
            when(jwtProvider.getEmailFromToken(refreshToken)).thenReturn("yasir@mail.com");
            when(authUserPort.findByEmail("yasir@mail.com")).thenReturn(Optional.of(user));
            when(jwtProvider.generateAccessToken(user.email(),user.role())).thenReturn("access-token");

            AuthTokenResponse authTokenResponse = authService.refresh(refreshToken);

            assertThat(authTokenResponse).isNotNull();
            assertThat(authTokenResponse.refreshToken()).isEqualTo(refreshToken);
            assertThat(authTokenResponse.accessToken()).isEqualTo("access-token");

            verify(jwtProvider).validateToken(refreshToken);
            verify(jwtProvider).getTypeFromToken(refreshToken);
            verify(jwtProvider).getEmailFromToken(refreshToken);
            verify(authUserPort).findByEmail("yasir@mail.com");
            verify(jwtProvider).generateAccessToken(user.email(),user.role());

        }

        @Test
        @DisplayName("should throw IllegalArgumentException when refresh token is invalid")
        void shouldThrowExceptionIllegalArgumentExceptionWhenRefreshTokenIsInvalid(){

            String refreshToken = "ajsgdyuagyfaeuyfhıea";

            when(jwtProvider.validateToken(refreshToken)).thenReturn(false);

            assertThatThrownBy(()-> authService.refresh(refreshToken)).
                    isInstanceOf(IllegalArgumentException.class);

            verify(jwtProvider).validateToken(refreshToken);
            verify(jwtProvider,never()).getEmailFromToken(anyString());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when refresh token is not REFRESH")
        void shouldThrowExceptionIllegalArgumentExceptionWhenRefreshTokenIsExpired(){

            String refreshToken = "ajsgdyuagyfaeuyfhıea";


            when(jwtProvider.validateToken(refreshToken)).thenReturn(true);
            when(jwtProvider.getTypeFromToken(refreshToken)).thenReturn("ACCESS");

            assertThatThrownBy(()-> authService.refresh(refreshToken)).
                    isInstanceOf(IllegalArgumentException.class);

            verify(jwtProvider).validateToken(refreshToken);
            verify(jwtProvider).getTypeFromToken(refreshToken);
            verify(jwtProvider,never()).getEmailFromToken(anyString());

        }

        @Test
        @DisplayName("should throw IllegalArgumentException when user is not found by email")
        void shouldThrowIllegalArgumentExceptionWhenUserIsNotFoundByEmail(){

            String refreshToken = "ajsgdyuagyfaeuyfhıea";

            when(jwtProvider.validateToken(refreshToken)).thenReturn(true);
            when(jwtProvider.getTypeFromToken(refreshToken)).thenReturn("REFRESH");
            when(jwtProvider.getEmailFromToken(refreshToken)).thenReturn("yasir@mail.com");
            when(authUserPort.findByEmail("yasir@mail.com")).thenReturn(Optional.empty());

            assertThatThrownBy(()-> authService.refresh(refreshToken))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(jwtProvider).validateToken(refreshToken);
            verify(jwtProvider).getTypeFromToken(refreshToken);
            verify(jwtProvider).getEmailFromToken(refreshToken);
            verify(authUserPort).findByEmail("yasir@mail.com");
            verify(jwtProvider,never()).generateAccessToken(anyString(),anyString());

        }




    }


}
