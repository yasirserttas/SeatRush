package com.seatrush.security;

import java.util.Optional;
import java.util.UUID;

public interface AuthUserPort {
    Optional<AuthUserView> findByEmail(String email);
    UUID createUser(String username, String email, String password, String firstName, String lastName);
}
