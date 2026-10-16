package com.seatrush.user.infrastructure.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "First name cannot be blank.")
        @Size(max = 100, message = "First name cannot exceed 100 characters.")
        String firstName,

        @NotBlank(message = "Last name cannot be blank.")
        @Size(max = 100, message = "Last name cannot exceed 100 characters.")
        String lastName,

        @NotBlank(message = "Email address cannot be blank.")
        @Email(message = "Please provide a valid email address.")
        @Size(max = 255, message = "Email address cannot exceed 255 characters.")
        String email
) {
}
