package com.seatrush.user.application.service;

import com.seatrush.user.application.command.ChangePasswordCommand;
import com.seatrush.user.application.command.CreateUserCommand;
import com.seatrush.user.application.command.UpdateUserCommand;
import com.seatrush.user.application.port.in.UserCommandUseCase;
import com.seatrush.user.application.port.out.UserRepository;
import com.seatrush.user.domain.exception.InvalidUserStateException;
import com.seatrush.user.domain.exception.UserNotFoundException;
import com.seatrush.user.domain.model.User;
import com.seatrush.user.domain.model.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

public class UserCommandService implements UserCommandUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserCommandService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UUID create(CreateUserCommand command) {
        if (command.role() == UserRole.ADMIN) {
            throw new IllegalArgumentException("ADMIN role cannot be created directly.");
        }

        String normalizedEmail = command.email() != null ? command.email().trim().toLowerCase() : null;
        String normalizedUsername = command.username() != null ? command.username().trim() : null;

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new InvalidUserStateException("This email address is already in use: " + command.email());
        }
        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new InvalidUserStateException("This username is already in use: " + command.username());
        }

        String encodedPassword = passwordEncoder.encode(command.password());

        User user = User.create(
                normalizedUsername,
                normalizedEmail,
                encodedPassword,
                command.firstName(),
                command.lastName(),
                command.role()
        );

        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }

    @Override
    public void update(UpdateUserCommand command) {
        User user = userRepository.findById(command.id())
                .orElseThrow(() -> new UserNotFoundException(command.id()));

        String normalizedEmail = command.email() != null ? command.email().trim().toLowerCase() : null;

        if (!user.getEmail().equalsIgnoreCase(normalizedEmail) && userRepository.existsByEmail(normalizedEmail)) {
            throw new InvalidUserStateException("This email address is already in use: " + command.email());
        }

        user.updateProfile(command.firstName(), command.lastName(), normalizedEmail);
        userRepository.save(user);
    }

    @Override
    public void changePassword(ChangePasswordCommand command) {
        User user = userRepository.findById(command.id())
                .orElseThrow(() -> new UserNotFoundException(command.id()));

        if (!passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
            throw new InvalidUserStateException("Current password is incorrect.");
        }

        if (passwordEncoder.matches(command.newPassword(), user.getPassword())) {
            throw new InvalidUserStateException("New password cannot be the same as current password.");
        }

        String encodedPassword = passwordEncoder.encode(command.newPassword());

        user.changePassword(encodedPassword);
        userRepository.save(user);
    }

    @Override
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userRepository.delete(user.getId());
    }

    @Override
    public void activate(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.activate();
        userRepository.save(user);
    }

    @Override
    public void deactivate(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.deactivate();
        userRepository.save(user);
    }

    @Override
    public void block(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.block();
        userRepository.save(user);
    }
}
