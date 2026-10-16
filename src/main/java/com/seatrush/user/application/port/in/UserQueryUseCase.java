package com.seatrush.user.application.port.in;

import com.seatrush.user.application.query.UserView;

import java.util.List;
import java.util.UUID;

public interface UserQueryUseCase {
    UserView getById(UUID id);
    UserView getByEmail(String email);
    UserView getByUsername(String username);
    List<UserView> getAll();
}
