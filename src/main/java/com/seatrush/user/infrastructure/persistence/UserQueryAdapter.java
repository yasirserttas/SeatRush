package com.seatrush.user.infrastructure.persistence;

import com.seatrush.user.application.port.out.UserQueryPort;
import com.seatrush.user.application.query.UserView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserQueryAdapter implements UserQueryPort {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<UserView> findById(UUID id) {
        return springDataUserRepository.findById(id).map(userMapper::toView);
    }

    @Override
    public Optional<UserView> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return springDataUserRepository.findByEmailIgnoreCase(email.trim()).map(userMapper::toView);
    }

    @Override
    public Optional<UserView> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return springDataUserRepository.findByUsernameIgnoreCase(username.trim()).map(userMapper::toView);
    }

    @Override
    public List<UserView> findAll() {
        return springDataUserRepository.findAll().stream().map(userMapper::toView).toList();
    }
}
