package com.seatrush.user.application.port.out;

import com.seatrush.user.application.query.UserView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserQueryPort {
    Optional<UserView> findById(UUID id);
    Optional<UserView> findByEmail(String email);
    Optional<UserView> findByUsername(String username);
    List<UserView> findAll();
}
