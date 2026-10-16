package com.seatrush.user.application.port.out;

import com.seatrush.user.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    void delete(UUID id);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
