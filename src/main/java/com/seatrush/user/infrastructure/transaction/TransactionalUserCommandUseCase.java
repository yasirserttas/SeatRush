package com.seatrush.user.infrastructure.transaction;

import com.seatrush.user.application.command.ChangePasswordCommand;
import com.seatrush.user.application.command.CreateUserCommand;
import com.seatrush.user.application.command.UpdateUserCommand;
import com.seatrush.user.application.port.in.UserCommandUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional
@RequiredArgsConstructor
public class TransactionalUserCommandUseCase implements UserCommandUseCase {

    private final UserCommandUseCase delegate;

    @Override
    public UUID create(CreateUserCommand command) {
        return delegate.create(command);
    }

    @Override
    public void update(UpdateUserCommand command) {
        delegate.update(command);
    }

    @Override
    public void changePassword(ChangePasswordCommand command) {
        delegate.changePassword(command);
    }

    @Override
    public void delete(UUID id) {
        delegate.delete(id);
    }

    @Override
    public void activate(UUID id) {
        delegate.activate(id);
    }

    @Override
    public void deactivate(UUID id) {
        delegate.deactivate(id);
    }

    @Override
    public void block(UUID id) {
        delegate.block(id);
    }
}
