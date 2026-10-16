package com.seatrush.security.infrastructure.userdetails;

import com.seatrush.security.AuthUserPort;
import com.seatrush.security.AuthUserView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTests {

    @Mock
    private AuthUserPort authUserPort;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("should load user and return UserDetails with ROLE_ prefix when user exists")
    void shouldLoadUserAndReturnUserDetailsWhenUserExists(){

        String email = "yasir@mail.com";

        AuthUserView user = new AuthUserView(
                UUID.randomUUID(),
                email,
                "hash98489494%+&+%%+^2",
                "ADMIN",
                "ACTIVE"
        );

        when(authUserPort.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo("hash98489494%+&+%%+^2");


        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");

        verify(authUserPort).findByEmail(email);

    }

    @Test
    @DisplayName("should throw UsernameNotFoundException when user is not found by email")
    void shouldThrowUsernameNotFoundExceptionWhenUserIsNotFoundByEmail(){

        String email = "yasir@mail.com";

        when(authUserPort.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(()->userDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with email: " + email);

        verify(authUserPort).findByEmail(email);

    }


}
