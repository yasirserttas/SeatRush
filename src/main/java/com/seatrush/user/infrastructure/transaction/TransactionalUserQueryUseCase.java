package com.seatrush.user.infrastructure.transaction;

import com.seatrush.user.application.port.in.UserQueryUseCase;
import com.seatrush.user.application.query.UserView;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionalUserQueryUseCase implements UserQueryUseCase {

    private final UserQueryUseCase delegate;

    @Override
    public UserView getById(UUID id) {
        return delegate.getById(id);
    }

    @Override
    public UserView getByEmail(String email) {
        return delegate.getByEmail(email);
    }

    @Override
    public UserView getByUsername(String username) {
        return delegate.getByUsername(username);
    }

    @Override
    public List<UserView> getAll() {
        return delegate.getAll();
    }
}
