package com.seatrush.user.application.port.in;

import com.seatrush.user.application.command.ChangePasswordCommand;
import com.seatrush.user.application.command.CreateUserCommand;
import com.seatrush.user.application.command.UpdateUserCommand;

import java.util.UUID;

public interface UserCommandUseCase {
    UUID create(CreateUserCommand command);
    void update(UpdateUserCommand command);
    void changePassword(ChangePasswordCommand command);
    void delete(UUID id);
    void activate(UUID id);
    void deactivate(UUID id);
    void block(UUID id);
}
