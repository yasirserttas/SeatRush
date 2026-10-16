package com.seatrush.user.infrastructure.web;

import com.seatrush.user.application.port.in.UserQueryUseCase;
import com.seatrush.user.application.query.UserView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserQueryController {

    private final UserQueryUseCase userQueryUseCase;

    @GetMapping("/{id}")
    public UserView getById(@PathVariable UUID id) {
        return userQueryUseCase.getById(id);
    }

    @GetMapping("/by-email")
    public UserView getByEmail(@RequestParam String email) {
        return userQueryUseCase.getByEmail(email);
    }

    @GetMapping("/by-username")
    public UserView getByUsername(@RequestParam String username) {
        return userQueryUseCase.getByUsername(username);
    }

    @GetMapping
    public List<UserView> getAll() {
        return userQueryUseCase.getAll();
    }
}
