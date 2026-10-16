package com.seatrush.security.infrastructure.jwt;

import com.seatrush.security.infrastructure.config.JwtProperties;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class JwtProviderTest {

    private JwtProvider jwtProvider;

    private static final String TEST_SECRET =
            "bXlTdXBlclNlY3VyZVNlYXRSdXNoSldUS2V5MjAyNlZlcnlMb25nQW5kU2VjdXJl";
    private static final long ACCESS_EXPIRATION = 900000L;       //
    private static final long REFRESH_EXPIRATION = 604800000L;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(TEST_SECRET, ACCESS_EXPIRATION, REFRESH_EXPIRATION);
        jwtProvider = new JwtProvider(properties);
    }


    @Nested
    class GenerateAccessToken{

        @Test
        @DisplayName("should generate valid access token with correct claims")
        void shouldGenerateValidAccessTokenWithCorrectClaims(){

            String email = "yasir@mail.com";
            String role = "ADMIN";

            String token = jwtProvider.generateAccessToken(email,role);

            assertThat(token).isNotBlank();
            assertThat(jwtProvider.validateToken(token)).isTrue();
            assertThat(jwtProvider.getEmailFromToken(token)).isEqualTo("yasir@mail.com");
            assertThat(jwtProvider.getRoleFromToken(token)).isEqualTo("ADMIN");
            assertThat(jwtProvider.getTypeFromToken(token)).isEqualTo("ACCESS");
        }

    }

    @Nested
    class GenerateRefreshToken{

        @Test
        @DisplayName("should generate valid refresh token with correct claims")
        void shouldGenerateValidRefreshTokenWithCorrectClaims(){

            String email = "yasir@mail.com";
            String role = "ADMIN";

            String token = jwtProvider.generateRefreshToken(email,role);

            assertThat(token).isNotBlank();
            assertThat(jwtProvider.validateToken(token)).isTrue();
            assertThat(jwtProvider.getEmailFromToken(token)).isEqualTo("yasir@mail.com");
            assertThat(jwtProvider.getRoleFromToken(token)).isEqualTo("ADMIN");
            assertThat(jwtProvider.getTypeFromToken(token)).isEqualTo("REFRESH");
        }

    }


    @Nested
    class ValidateToken{


        @Test
        @DisplayName("should return true when token is valid and not expired")
        void shouldReturnTrueWhenTokenIsValidAndNotExpired(){

            String token = jwtProvider.generateRefreshToken("test@mail.com","ADMIN");

            boolean isValid = jwtProvider.validateToken(token);

            assertThat(isValid).isTrue();
        }


        @Test
        @DisplayName("should return false when token is malformed or invalid")
        void shouldReturnFalseWhenTokenIsMalformedOrValid(){

            String token = "gecersiz.bir.tokenim.ben";

            boolean isValid = jwtProvider.validateToken(token);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("should return false when token is signed with different secret key")
        void shouldReturnFalseWhenTokenIsSignedWithDifferentSecretKey(){

            String differentSecret = "YW5vdGhlclNlY3VyZVNlYXRSdXNoSldUS2V5MjAyNlZlcnlMb25nU2VjcmV0";

            JwtProperties otherProperties =
                    new JwtProperties(differentSecret, ACCESS_EXPIRATION, REFRESH_EXPIRATION);

            JwtProvider foreignProvider = new JwtProvider(otherProperties);

            String foreignToken = foreignProvider.generateAccessToken("hacker@mail.com","ADMIN");

            boolean isValid = jwtProvider.validateToken(foreignToken);

            assertThat(isValid).isFalse();

        }

        @Test
        @DisplayName("should return false when token is expired")
        void shouldReturnFalseWhenTokenIsExpired() {
            JwtProperties expiredProperties = new
                    JwtProperties(TEST_SECRET, -1000L, -1000L);
            JwtProvider expiredProvider = new JwtProvider(expiredProperties);
            String expiredToken = expiredProvider.generateAccessToken("expired@mail.com", "CUSTOMER");
            boolean isValid = jwtProvider.validateToken(expiredToken);
            assertThat(isValid).isFalse();
        }


    }

}