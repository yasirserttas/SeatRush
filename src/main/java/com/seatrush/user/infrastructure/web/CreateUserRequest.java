package com.seatrush.user.infrastructure.web;

import com.seatrush.user.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Username cannot be blank.")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters.")
        String username,

        @NotBlank(message = "Email address cannot be blank.")
        @Email(message = "Please provide a valid email address.")
        @Size(max = 255, message = "Email address cannot exceed 255 characters.")
        String email,

        @NotBlank(message = "Password cannot be blank.")
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters.")
        String password,

        @NotBlank(message = "First name cannot be blank.")
        @Size(max = 100, message = "First name cannot exceed 100 characters.")
        String firstName,

        @NotBlank(message = "Last name cannot be blank.")
        @Size(max = 100, message = "Last name cannot exceed 100 characters.")
        String lastName,

        UserRole role
) {
}
