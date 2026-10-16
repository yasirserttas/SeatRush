package com.seatrush.security.infrastructure.config;

import com.seatrush.security.AuthUserPort;
import com.seatrush.security.application.port.in.AuthUseCase;
import com.seatrush.security.application.service.AuthService;
import com.seatrush.security.infrastructure.jwt.JwtAuthenticationFilter;
import com.seatrush.security.infrastructure.jwt.JwtProvider;
import com.seatrush.security.infrastructure.userdetails.CustomUserDetailsService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public AuthUseCase authUseCase(AuthUserPort authUserPort,
                                   PasswordEncoder passwordEncoder, JwtProvider jwtProvider){

        return new AuthService(authUserPort,passwordEncoder,jwtProvider);
    }

    @Bean
    public UserDetailsService userDetailsService(AuthUserPort authUserPort) {
        return new CustomUserDetailsService(authUserPort);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        return httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-ui.html").permitAll()

                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/users/*/block").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/events").hasAnyRole("ADMIN", "ORGANIZER")

                        .anyRequest().authenticated()

                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }


}
