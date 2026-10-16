package com.seatrush.user.application.service;

import com.seatrush.user.application.port.in.UserQueryUseCase;
import com.seatrush.user.application.port.out.UserQueryPort;
import com.seatrush.user.application.query.UserView;
import com.seatrush.user.domain.exception.UserNotFoundException;

import java.util.List;
import java.util.UUID;

public class UserQueryService implements UserQueryUseCase {

    private final UserQueryPort userQueryPort;

    public UserQueryService(UserQueryPort userQueryPort) {
        this.userQueryPort = userQueryPort;
    }

    @Override
    public UserView getById(UUID id) {
        return userQueryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public UserView getByEmail(String email) {
        return userQueryPort.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    @Override
    public UserView getByUsername(String username) {
        return userQueryPort.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    @Override
    public List<UserView> getAll() {
        return userQueryPort.findAll();
    }
}
