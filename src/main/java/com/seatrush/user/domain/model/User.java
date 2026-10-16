package com.seatrush.user.domain.model;

import com.seatrush.user.domain.exception.InvalidUserStateException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

public class User {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 128;

    private final UUID id;
    private final String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private UserRole role;
    private UserStatus status;
    private final LocalDateTime createdAt;

    private User(
            UUID id,
            String username,
            String email,
            String password,
            String firstName,
            String lastName,
            UserRole role,
            UserStatus status,
            LocalDateTime createdAt
    ) {
        validateId(id);
        validateUsername(username);
        validateEmail(email);
        validatePassword(password);
        validateFirstName(firstName);
        validateLastName(lastName);
        validateRole(role);
        validateStatus(status);
        validateCreatedAt(createdAt);

        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static User create(
            String username,
            String email,
            String password,
            String firstName,
            String lastName,
            UserRole role
    ) {
        return new User(
                UUID.randomUUID(),
                username != null ? username.trim() : null,
                email != null ? email.trim().toLowerCase() : null,
                password,
                firstName != null ? firstName.trim() : null,
                lastName != null ? lastName.trim() : null,
                role != null ? role : UserRole.CUSTOMER,
                UserStatus.ACTIVE,
                LocalDateTime.now()
        );
    }

    public static User restore(
            UUID id,
            String username,
            String email,
            String password,
            String firstName,
            String lastName,
            UserRole role,
            UserStatus status,
            LocalDateTime createdAt
    ) {
        return new User(
                id,
                username,
                email,
                password,
                firstName,
                lastName,
                role,
                status,
                createdAt
        );
    }


    public void updateProfile(
            String firstName,
            String lastName,
            String email
    ) {
        validateCanUpdateProfile();
        validateFirstName(firstName);
        validateLastName(lastName);
        validateEmail(email);

        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.email = email.trim().toLowerCase();
    }

    public void changePassword(String newPassword) {
        validateCanChangePassword();
        validatePassword(newPassword);
        if (this.password != null && this.password.equals(newPassword)) {
            throw new InvalidUserStateException("New password cannot be the same as current password.");
        }

        this.password = newPassword;
    }

    public void activate() {
        validateCanActivate();
        this.status = UserStatus.ACTIVE;
    }

    public void deactivate() {
        validateCanDeactivate();
        this.status = UserStatus.INACTIVE;
    }

    public void block() {
        validateCanBlock();
        this.status = UserStatus.BLOCKED;
    }

    // ==========================================
    // STATUS QUERY METHODS
    // ==========================================

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean isInactive() {
        return status == UserStatus.INACTIVE;
    }

    public boolean isBlocked() {
        return status == UserStatus.BLOCKED;
    }

    // ==========================================
    // STATE TRANSITION VALIDATIONS
    // ==========================================

    private void validateCanUpdateProfile() {
        if (this.status == UserStatus.BLOCKED) {
            throw new InvalidUserStateException("Blocked user profile cannot be updated.");
        }
    }

    private void validateCanChangePassword() {
        if (this.status == UserStatus.BLOCKED) {
            throw new InvalidUserStateException("Blocked user password cannot be changed.");
        }
    }

    private void validateCanActivate() {

        if (this.status == UserStatus.ACTIVE) {
            throw new InvalidUserStateException("Active user cannot be activated again.");
        }
    }

    private void validateCanDeactivate() {
        if (this.status == UserStatus.INACTIVE) {
            throw new InvalidUserStateException("Inactive user cannot be deactivated again.");
        }
    }

    private void validateCanBlock() {
        if (this.status == UserStatus.BLOCKED) {
            throw new InvalidUserStateException("Blocked user cannot be blocked again.");
        }
    }

    // ==========================================
    // INVARIANT / FIELD VALIDATIONS
    // ==========================================

    private static void validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null.");
        }
    }

    private static void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank.");
        }
        if (username.trim().length() > 100) {
            throw new IllegalArgumentException("Username cannot exceed 100 characters.");
        }
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank() || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("A valid email address must be provided.");
        }
        if (email.trim().length() > 255) {
            throw new IllegalArgumentException("Email address cannot exceed 255 characters.");
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.isBlank() || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password cannot exceed " + MAX_PASSWORD_LENGTH + " characters.");
        }
    }

    private static void validateFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be blank.");
        }
        if (firstName.trim().length() > 100) {
            throw new IllegalArgumentException("First name cannot exceed 100 characters.");
        }
    }

    private static void validateLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be blank.");
        }
        if (lastName.trim().length() > 100) {
            throw new IllegalArgumentException("Last name cannot exceed 100 characters.");
        }
    }

    private static void validateRole(UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("User role cannot be null.");
        }
    }

    private static void validateStatus(UserStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("User status cannot be null.");
        }
    }

    private static void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("Creation date cannot be null.");
        }
    }


    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
