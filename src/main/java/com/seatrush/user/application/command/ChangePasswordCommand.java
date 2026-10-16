package com.seatrush.user.application.command;

import java.util.UUID;

public record ChangePasswordCommand(
        UUID id,
        String currentPassword,
        String newPassword
) {
}

