package com.seatrush.user.infrastructure.config;

import com.seatrush.security.infrastructure.config.PasswordEncoderConfig;
import com.seatrush.user.application.port.in.UserCommandUseCase;
import com.seatrush.user.application.port.in.UserQueryUseCase;
import com.seatrush.user.application.port.out.UserQueryPort;
import com.seatrush.user.application.port.out.UserRepository;
import com.seatrush.user.application.service.UserCommandService;
import com.seatrush.user.application.service.UserQueryService;
import com.seatrush.user.infrastructure.transaction.TransactionalUserCommandUseCase;
import com.seatrush.user.infrastructure.transaction.TransactionalUserQueryUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserModuleConfig {

    @Bean
    public UserCommandUseCase userCommandUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        UserCommandService service = new UserCommandService(userRepository,passwordEncoder);
        return new TransactionalUserCommandUseCase(service);
    }

    @Bean
    public UserQueryUseCase userQueryUseCase(UserQueryPort userQueryPort) {
        UserQueryService service = new UserQueryService(userQueryPort);
        return new TransactionalUserQueryUseCase(service);
    }
}
