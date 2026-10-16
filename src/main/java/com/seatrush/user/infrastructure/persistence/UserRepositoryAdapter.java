package com.seatrush.user.infrastructure.persistence;

import com.seatrush.user.application.port.out.UserRepository;
import com.seatrush.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        UserJpaEntity entity = userMapper.toJpaEntity(user);
        return userMapper.fromEntity(springDataUserRepository.save(entity));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepository.findById(id).map(userMapper::fromEntity);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return springDataUserRepository.findByEmailIgnoreCase(email.trim()).map(userMapper::fromEntity);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return springDataUserRepository.findByUsernameIgnoreCase(username.trim()).map(userMapper::fromEntity);
    }

    @Override
    public void delete(UUID id) {
        springDataUserRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return email != null && springDataUserRepository.existsByEmailIgnoreCase(email.trim());
    }

    @Override
    public boolean existsByUsername(String username) {
        return username != null && springDataUserRepository.existsByUsernameIgnoreCase(username.trim());
    }
}
