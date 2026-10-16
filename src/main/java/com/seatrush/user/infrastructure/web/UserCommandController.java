package com.seatrush.user.infrastructure.web;

import com.seatrush.user.application.command.ChangePasswordCommand;
import com.seatrush.user.application.command.CreateUserCommand;
import com.seatrush.user.application.command.UpdateUserCommand;
import com.seatrush.user.application.port.in.UserCommandUseCase;
import com.seatrush.user.domain.model.UserRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserCommandController {

    private final UserCommandUseCase userCommandUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponse create(@Valid @RequestBody CreateUserRequest request) {
        UUID userId = userCommandUseCase.create(
                new CreateUserCommand(
                        request.username(),
                        request.email(),
                        request.password(),
                        request.firstName(),
                        request.lastName(),
                        request.role() != null ? request.role() : UserRole.CUSTOMER
                )
        );

        return new CreateUserResponse(userId);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        userCommandUseCase.update(
                new UpdateUserCommand(
                        id,
                        request.firstName(),
                        request.lastName(),
                        request.email()
                )
        );
    }

    @PatchMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @PathVariable UUID id,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userCommandUseCase.changePassword(
                new ChangePasswordCommand(id, request.currentPassword(), request.newPassword())
        );
    }

    @PatchMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(@PathVariable UUID id) {
        userCommandUseCase.activate(id);
    }

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable UUID id) {
        userCommandUseCase.deactivate(id);
    }

    @PatchMapping("/{id}/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void block(@PathVariable UUID id) {
        userCommandUseCase.block(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        userCommandUseCase.delete(id);
    }
}
