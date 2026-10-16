package com.seatrush.security.infrastructure.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Unit Tests")
public class JwtAuthenticationFilterTests {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("DoFilterInternal Tests")
    class DoFilterInternal {

        @Test
        @DisplayName("should continue filter chain and not authenticate when authorization header is missing")
        void shouldContinueFilterChainWhenHeaderIsMissing() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn(null);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(jwtProvider, never()).validateToken(anyString());
        }

        @Test
        @DisplayName("should continue filter chain and not authenticate when authorization header does not start with Bearer")
        void shouldContinueFilterChainWhenHeaderDoesNotStartWithBearer() throws ServletException, IOException {
            when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(jwtProvider, never()).validateToken(anyString());
        }

        @Test
        @DisplayName("should authenticate user and set security context when token is valid")
        void shouldAuthenticateAndSetSecurityContextWhenTokenIsValid() throws ServletException, IOException {
            String token = "valid.jwt.token";
            String email = "yasir@mail.com";
            UserDetails userDetails = new User(
                    email,
                    "password",
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtProvider.validateToken(token)).thenReturn(true);
            when(jwtProvider.getEmailFromToken(token)).thenReturn(email);
            when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getName()).isEqualTo(email);
            assertThat(authentication.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_ADMIN");
        }

        @Test
        @DisplayName("should continue filter chain and not authenticate when token is invalid")
        void shouldContinueFilterChainWhenTokenIsInvalid() throws ServletException, IOException {
            String token = "invalid.jwt.token";

            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(jwtProvider.validateToken(token)).thenReturn(false);

            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(userDetailsService, never()).loadUserByUsername(anyString());
        }
    }
}
