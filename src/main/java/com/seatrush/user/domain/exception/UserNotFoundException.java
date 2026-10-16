package com.seatrush.user.domain.exception;

import java.util.UUID;

public class UserNotFoundException extends UserDomainException {

    public UserNotFoundException(UUID id) {
        super("User not found with id: " + id);
    }

    public UserNotFoundException(String identifier) {
        super("User not found: " + identifier);
    }
}
