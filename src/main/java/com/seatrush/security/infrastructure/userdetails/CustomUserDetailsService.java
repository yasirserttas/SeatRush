package com.seatrush.security.infrastructure.userdetails;

import com.seatrush.security.AuthUserPort;
import com.seatrush.security.AuthUserView;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public class CustomUserDetailsService implements UserDetailsService {

    private final AuthUserPort authUserPort;

    public CustomUserDetailsService(AuthUserPort authUserPort) {
        this.authUserPort = authUserPort;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        AuthUserView authUserView = authUserPort.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("User not found with email: " + email));

        return new User(
                authUserView.email(),
                authUserView.password(),
                List.of(new SimpleGrantedAuthority("ROLE_"+authUserView.role()))
        );
    }
}
