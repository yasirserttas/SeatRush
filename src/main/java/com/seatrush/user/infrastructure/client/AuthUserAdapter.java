package com.seatrush.user.infrastructure.client;

import com.seatrush.security.AuthUserPort;
import com.seatrush.security.AuthUserView;
import com.seatrush.user.application.command.CreateUserCommand;
import com.seatrush.user.application.port.in.UserCommandUseCase;
import com.seatrush.user.application.port.out.UserRepository;
import com.seatrush.user.domain.model.User;
import com.seatrush.user.domain.model.UserRole;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AuthUserAdapter  implements AuthUserPort {

    private final UserRepository userRepository;
    private final UserCommandUseCase userCommandUseCase;

    public AuthUserAdapter(UserRepository userRepository, UserCommandUseCase userCommandUseCase) {
        this.userRepository = userRepository;
        this.userCommandUseCase = userCommandUseCase;
    }


    @Override
    public Optional<AuthUserView> findByEmail(String email) {

        Optional<User> optionalUser = userRepository.findByEmail(email);

        return optionalUser.map(user -> new AuthUserView(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().name(),
                user.getStatus().name()
        ));
    }

    @Override
    public UUID createUser(String username, String email, String password, String firstName, String lastName) {

        return userCommandUseCase.create(new CreateUserCommand(username,email,password,
                firstName,lastName, UserRole.CUSTOMER));

    }
}
