package com.seatrush.user.domain.model;

import com.seatrush.user.domain.exception.InvalidUserStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("User Domain Model Unit Tests")
public class UserTest {

    private final UUID id = UUID.randomUUID();
    private final String username = "username";
    private final String email = "yasirrserttas@gmail.com";
    private final String password = "password";
    private final String firstName = "firstName";
    private final String lastName = "lastName";
    private final LocalDateTime created = LocalDateTime.of(2020, 1, 1, 0, 0, 0);

    @Nested
    @DisplayName("Creation Tests")
    class Create {

        @Test
        @DisplayName("Should create active user with valid parameters")
        void shouldCreateActiveUserWithValidParameters() {
            User user = User.create(username, email, password, firstName, lastName, UserRole.ADMIN);

            assertThat(user.getId()).isNotNull();
            assertThat(user.getUsername()).isEqualTo(username);
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getPassword()).isEqualTo(password);
            assertThat(user.getFirstName()).isEqualTo(firstName);
            assertThat(user.getLastName()).isEqualTo(lastName);
            assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
            assertThat(user.isActive()).isTrue();
            assertThat(user.getCreatedAt()).isNotNull();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Should throw exception when username is blank or null")
        void shouldThrowExceptionWhenUsernameIsBlankOrNull(String testUsername) {
            assertThatThrownBy(() -> User.create(testUsername, email, password, firstName, lastName, UserRole.ADMIN))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "invalidEmail", "test@", "@examples.com"})
        @DisplayName("Should throw exception when email is invalid")
        void shouldThrowExceptionWhenEmailIsInvalid(String testEmail) {
            assertThatThrownBy(() -> User.create(username, testEmail, password, firstName, lastName, UserRole.ADMIN))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"\t", "1", "12", "123", "1234", "12345"})
        @DisplayName("Should throw exception when password is blank, null or shorter than 6 chars")
        void shouldThrowExceptionWhenPasswordIsBlankOrNullOrTooShort(String testPassword) {
            assertThatThrownBy(() -> User.create(username, email, testPassword, firstName, lastName, UserRole.ADMIN))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should create user when password has minimum required length")
        void shouldCreateUserWhenPasswordHasMinimumLength() {
            User user = User.create(username, email, "123456", firstName, lastName, UserRole.ADMIN);

            assertThat(user.getPassword()).isEqualTo("123456");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Should throw exception when firstName is blank or null")
        void shouldThrowExceptionWhenFirstNameIsBlank(String testFirstname) {
            assertThatThrownBy(() -> User.create(username, email, password, testFirstname, lastName, UserRole.ADMIN))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Should throw exception when lastName is blank or null")
        void shouldThrowExceptionWhenLastNameIsBlank(String testLastname) {
            assertThatThrownBy(() -> User.create(username, email, password, firstName, testLastname, UserRole.ADMIN))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should default role to CUSTOMER when userRole is null")
        void shouldDefaultRoleToCustomerWhenUserRoleIsNull() {
            User user = User.create(username, email, password, firstName, lastName, null);

            assertThat(user.getRole()).isEqualTo(UserRole.CUSTOMER);
        }
    }

    @Nested
    @DisplayName("Restore Tests")
    class Restore {

        @Test
        @DisplayName("Should restore user with valid parameters")
        void shouldRestoreUserWithValidParameters() {
            User user = User.restore(
                    id,
                    username,
                    email,
                    password,
                    firstName,
                    lastName,
                    UserRole.CUSTOMER,
                    UserStatus.ACTIVE,
                    created
            );

            assertThat(user.getId()).isEqualTo(id);
            assertThat(user.getUsername()).isEqualTo(username);
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getPassword()).isEqualTo(password);
            assertThat(user.getFirstName()).isEqualTo(firstName);
            assertThat(user.getLastName()).isEqualTo(lastName);
            assertThat(user.getRole()).isEqualTo(UserRole.CUSTOMER);
            assertThat(user.isActive()).isTrue();
            assertThat(user.getCreatedAt()).isEqualTo(created);
        }

        @Test
        @DisplayName("Should throw exception when restored id is null")
        void shouldThrowExceptionWhenRestoredIdIsNull() {
            assertThatThrownBy(() -> User.restore(
                    null,
                    username,
                    email,
                    password,
                    firstName,
                    lastName,
                    UserRole.CUSTOMER,
                    UserStatus.ACTIVE,
                    created
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Should throw exception when restored username is blank or null")
        void shouldThrowExceptionWhenRestoredUsernameIsBlankOrNull(String testUsername) {
            assertThatThrownBy(() -> User.restore(
                    id,
                    testUsername,
                    email,
                    password,
                    firstName,
                    lastName,
                    UserRole.CUSTOMER,
                    UserStatus.ACTIVE,
                    created
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "invalidEmail", "test@", "@examples.com"})
        @DisplayName("Should throw exception when restored email is invalid")
        void shouldThrowExceptionWhenRestoredEmailIsInvalid(String testEmail) {
            assertThatThrownBy(() -> User.restore(
                    id,
                    username,
                    testEmail,
                    password,
                    firstName,
                    lastName,
                    UserRole.CUSTOMER,
                    UserStatus.ACTIVE,
                    created
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"\t", "1", "12", "123", "1234", "12345"})
        @DisplayName("Should throw exception when restored password is blank, null or shorter than 6 chars")
        void shouldThrowExceptionWhenRestorePasswordIsBlankOrNullOrTooShort(String testPassword) {
            assertThatThrownBy(() -> User.restore(
                    id,
                    username,
                    email,
                    testPassword,
                    firstName,
                    lastName,
                    UserRole.ADMIN,
                    UserStatus.ACTIVE,
                    created
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should restore user when password has minimum required length")
        void shouldRestoreUserWhenPasswordHasMinimumLength() {
            User user = User.restore(
                    id,
                    username,
                    email,
                    "123456",
                    firstName,
                    lastName,
                    UserRole.ADMIN,
                    UserStatus.ACTIVE,
                    created
            );

            assertThat(user.getPassword()).isEqualTo("123456");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Should throw exception when restored firstName is blank or null")
        void shouldThrowExceptionWhenRestoredFirstNameIsBlankOrNull(String testFirstname) {
            assertThatThrownBy(() -> User.restore(
                    id,
                    username,
                    email,
                    "123456",
                    testFirstname,
                    lastName,
                    UserRole.ADMIN,
                    UserStatus.ACTIVE,
                    created
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Should throw exception when restored lastName is blank or null")
        void shouldThrowExceptionWhenRestoredLastNameIsBlankOrNull(String testLastname) {
            assertThatThrownBy(() -> User.restore(
                    id,
                    username,
                    email,
                    "123456",
                    firstName,
                    testLastname,
                    UserRole.ADMIN,
                    UserStatus.ACTIVE,
                    created
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored userRole is null")
        void shouldThrowExceptionWhenRestoredUserRoleIsNull() {
            assertThatThrownBy(() -> User.restore(
                    id,
                    username,
                    email,
                    "123456",
                    firstName,
                    lastName,
                    null,
                    UserStatus.ACTIVE,
                    created
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored userStatus is null")
        void shouldThrowExceptionWhenRestoredUserStatusIsNull() {
            assertThatThrownBy(() -> User.restore(
                    id,
                    username,
                    email,
                    "123456",
                    firstName,
                    lastName,
                    UserRole.CUSTOMER,
                    null,
                    created
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored createdAt is null")
        void shouldThrowExceptionWhenRestoredCreatedAtIsNull() {
            assertThatThrownBy(() -> User.restore(
                    id,
                    username,
                    email,
                    "123456",
                    firstName,
                    lastName,
                    UserRole.CUSTOMER,
                    UserStatus.ACTIVE,
                    null
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @EnumSource(UserRole.class)
        @DisplayName("Should restore user with different roles correctly")
        void shouldRestoreUserWithDifferentRoles(UserRole role) {
            User user = User.restore(
                    id,
                    username,
                    email,
                    password,
                    firstName,
                    lastName,
                    role,
                    UserStatus.ACTIVE,
                    created
            );

            assertThat(user.getRole()).isEqualTo(role);
        }

        @ParameterizedTest
        @EnumSource(UserStatus.class)
        @DisplayName("Should restore user with different statuses correctly")
        void shouldRestoreUserWithDifferentStatuses(UserStatus status) {
            User user = User.restore(
                    id,
                    username,
                    email,
                    password,
                    firstName,
                    lastName,
                    UserRole.CUSTOMER,
                    status,
                    created
            );

            assertThat(user.getStatus()).isEqualTo(status);
        }
    }

    @Nested
    @DisplayName("Update Profile Tests")
    class UpdateProfile {

        @Test
        @DisplayName("Should reject profile update when blocked user updates profile")
        void shouldRejectProfileUpdateWhenBlockedUserUpdatesProfile() {
            User user = User.create(username, email, password, firstName, lastName, UserRole.CUSTOMER);
            user.block();
            assertThat(user.getStatus()).isEqualTo(UserStatus.BLOCKED);

            assertThatThrownBy(() -> user.updateProfile("firstname", "lastname", "testemail@gmail.com"))
                    .isInstanceOf(InvalidUserStateException.class);
        }

        @ParameterizedTest
        @EnumSource(value = UserStatus.class, names = {"ACTIVE", "INACTIVE"})
        @DisplayName("Should update profile when user is not blocked")
        void shouldUpdateProfileWhenUserIsNotBlocked(UserStatus userStatus) {
            User user = withUserStatus(userStatus);
            user.updateProfile("firstname", "lastname", "testemail@gmail.com");

            assertThat(user.getStatus()).isEqualTo(userStatus);
            assertThat(user.getFirstName()).isEqualTo("firstname");
            assertThat(user.getLastName()).isEqualTo("lastname");
            assertThat(user.getEmail()).isEqualTo("testemail@gmail.com");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Should throw exception on profile update when firstName is null or blank")
        void shouldThrowExceptionWhenFirstNameIsNullOrBlank(String testFirstName) {
            User user = withUserStatus(UserStatus.ACTIVE);
            assertThatThrownBy(() -> user.updateProfile(testFirstName, lastName, email))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Should throw exception on profile update when lastName is null or blank")
        void shouldThrowExceptionWhenLastNameIsNullOrBlank(String testLastName) {
            User user = withUserStatus(UserStatus.ACTIVE);
            assertThatThrownBy(() -> user.updateProfile(firstName, testLastName, email))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "invalidEmail", "test@", "@examples.com"})
        @DisplayName("Should throw exception on profile update when email is invalid")
        void shouldThrowExceptionWhenEmailIsInvalid(String testEmail) {
            User user = withUserStatus(UserStatus.ACTIVE);
            assertThatThrownBy(() -> user.updateProfile(firstName, lastName, testEmail))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePassword {

        @Test
        @DisplayName("Should throw exception when changing password on BLOCKED user")
        void shouldThrowExceptionWhenUserStatusIsBlocked() {
            User user = withUserStatus(UserStatus.BLOCKED);

            assertThatThrownBy(() -> user.changePassword("123456789"))
                    .isInstanceOf(InvalidUserStateException.class);
        }

        @ParameterizedTest
        @EnumSource(value = UserStatus.class, names = {"ACTIVE", "INACTIVE"})
        @DisplayName("Should change password when user is not blocked")
        void shouldUserChangePasswordWhenUserStatusIsNotBlocked(UserStatus userStatus) {
            User user = withUserStatus(userStatus);
            user.changePassword("123456789");

            assertThat(user.getPassword()).isEqualTo("123456789");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"\t", "1", "12", "123", "1234", "12345"})
        @DisplayName("Should throw exception when new password is blank, null or shorter than 6 chars")
        void shouldThrowExceptionWhenPasswordIsNullOrBlankOrTooShort(String testPassword) {
            User user = withUserStatus(UserStatus.ACTIVE);

            assertThatThrownBy(() -> user.changePassword(testPassword))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should change password when password has minimum required length")
        void shouldUserChangePasswordWithMinLength() {
            User user = withUserStatus(UserStatus.ACTIVE);
            user.changePassword("123456");

            assertThat(user.getPassword()).isEqualTo("123456");
        }
    }

    @Nested
    @DisplayName("Activate Tests")
    class Activate {

        @Test
        @DisplayName("Should activate with valid parameters")
        void shouldActivateWithValidParameters() {
            User user = withUserStatus(UserStatus.INACTIVE);
            user.activate();

            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(user.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when activating an already active user")
        void shouldThrowExceptionWhenUserIsActivatedAgain() {
            User user = withUserStatus(UserStatus.ACTIVE);

            assertThatThrownBy(user::activate)
                    .isInstanceOf(InvalidUserStateException.class);
        }

        @Test
        @DisplayName("Should activate user when user is blocked")
        void shouldActiveUserWhenUserIsBlocked() {
            User user = withUserStatus(UserStatus.BLOCKED);
            user.activate();

            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(user.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Deactivate Tests")
    class Deactivate {

        @Test
        @DisplayName("Should deactivate user with valid parameters")
        void shouldDeactivateWithValidParameters() {
            User user = withUserStatus(UserStatus.ACTIVE);
            user.deactivate();

            assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
            assertThat(user.isInactive()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when deactivating an already inactive user")
        void shouldThrowExceptionWhenUserIsDeactivatedAgain() {
            User user = withUserStatus(UserStatus.INACTIVE);

            assertThatThrownBy(user::deactivate)
                    .isInstanceOf(InvalidUserStateException.class);
        }

        @Test
        @DisplayName("Should deactivate user when user is blocked")
        void shouldDeactivateUserWhenUserIsBlocked() {
            User user = withUserStatus(UserStatus.BLOCKED);
            user.deactivate();

            assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
            assertThat(user.isInactive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Block Tests")
    class Blocked {

        @ParameterizedTest
        @EnumSource(value = UserStatus.class, names = {"ACTIVE", "INACTIVE"})
        @DisplayName("Should block user when user is not blocked")
        void shouldBlockUserWhenUserIsNotBlocked(UserStatus status) {
            User user = withUserStatus(status);
            user.block();

            assertThat(user.getStatus()).isEqualTo(UserStatus.BLOCKED);
            assertThat(user.isBlocked()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when blocking an already blocked user")
        void shouldThrowExceptionWhenUserIsBlockedAgain() {
            User user = withUserStatus(UserStatus.BLOCKED);

            assertThatThrownBy(user::block)
                    .isInstanceOf(InvalidUserStateException.class);
        }
    }

    private User withUserStatus(UserStatus userStatus) {
        return User.restore(
                id,
                username,
                email,
                password,
                firstName,
                lastName,
                UserRole.CUSTOMER,
                userStatus,
                created
        );
    }
}
