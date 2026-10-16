package com.seatrush.user.application.service;

import com.seatrush.user.application.command.ChangePasswordCommand;
import com.seatrush.user.application.command.CreateUserCommand;
import com.seatrush.user.application.command.UpdateUserCommand;
import com.seatrush.user.application.port.out.UserRepository;
import com.seatrush.user.domain.exception.InvalidUserStateException;
import com.seatrush.user.domain.exception.UserNotFoundException;
import com.seatrush.user.domain.model.User;
import com.seatrush.user.domain.model.UserRole;
import com.seatrush.user.domain.model.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserCommandService Unit Tests")
class UserCommandServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserCommandService userCommandService;

    @Nested
    @DisplayName("Create Tests")
    class Create {

        @Test
        @DisplayName("should create, save user and return user id when command is valid")
        void shouldCreateSaveUserAndReturnUserIdWhenCommandIsValid() {
            CreateUserCommand command = new CreateUserCommand(
                    "johndoe",
                    "john@example.com",
                    "password123",
                    "John",
                    "Doe",
                    UserRole.CUSTOMER
            );

            when(passwordEncoder.encode(command.password())).thenReturn("encodedPassword123");
            when(userRepository.existsByEmail(command.email())).thenReturn(false);
            when(userRepository.existsByUsername(command.username())).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

            UUID actualUserId = userCommandService.create(command);

            verify(userRepository).existsByEmail(command.email());
            verify(userRepository).existsByUsername(command.username());
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getUsername()).isEqualTo("johndoe");
            assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
            assertThat(savedUser.getPassword()).isEqualTo("encodedPassword123");
            assertThat(savedUser.getFirstName()).isEqualTo("John");
            assertThat(savedUser.getLastName()).isEqualTo("Doe");
            assertThat(savedUser.getRole()).isEqualTo(UserRole.CUSTOMER);
            assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(actualUserId).isEqualTo(savedUser.getId());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when creating admin user")
        void shouldThrowIllegalArgumentExceptionWhenCreatingAdminUser() {
            CreateUserCommand command = new CreateUserCommand(
                    "admin",
                    "admin@example.com",
                    "password123",
                    "Admin",
                    "User",
                    UserRole.ADMIN
            );

            assertThatThrownBy(() -> userCommandService.create(command))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw InvalidUserStateException when email already exists")
        void shouldThrowInvalidUserStateExceptionWhenEmailAlreadyExists() {
            CreateUserCommand command = new CreateUserCommand(
                    "johndoe",
                    "john@example.com",
                    "password123",
                    "John",
                    "Doe",
                    UserRole.CUSTOMER
            );

            when(userRepository.existsByEmail(command.email())).thenReturn(true);

            assertThatThrownBy(() -> userCommandService.create(command))
                    .isInstanceOf(InvalidUserStateException.class)
                    .hasMessageContaining("This email address is already in use");

            verify(userRepository).existsByEmail(command.email());
            verify(userRepository, never()).existsByUsername(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw InvalidUserStateException when username already exists")
        void shouldThrowInvalidUserStateExceptionWhenUsernameAlreadyExists() {
            CreateUserCommand command = new CreateUserCommand(
                    "johndoe",
                    "john@example.com",
                    "password123",
                    "John",
                    "Doe",
                    UserRole.CUSTOMER
            );

            when(userRepository.existsByEmail(command.email())).thenReturn(false);
            when(userRepository.existsByUsername(command.username())).thenReturn(true);

            assertThatThrownBy(() -> userCommandService.create(command))
                    .isInstanceOf(InvalidUserStateException.class)
                    .hasMessageContaining("This username is already in use");

            verify(userRepository).existsByEmail(command.email());
            verify(userRepository).existsByUsername(command.username());
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Update Tests")
    class Update {

        @Test
        @DisplayName("should update profile and save when command is valid with unchanged email")
        void shouldUpdateProfileAndSaveWhenCommandIsValidWithUnchangedEmail() {
            UUID userId = UUID.randomUUID();
            User user = User.create("johndoe", "john@example.com", "password123", "John", "Doe", UserRole.CUSTOMER);

            UpdateUserCommand command = new UpdateUserCommand(userId, "Jane", "Smith", "john@example.com");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            userCommandService.update(command);

            assertThat(user.getFirstName()).isEqualTo("Jane");
            assertThat(user.getLastName()).isEqualTo("Smith");
            assertThat(user.getEmail()).isEqualTo("john@example.com");

            verify(userRepository).findById(userId);
            verify(userRepository, never()).existsByEmail(any());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should update profile and save when new email is not taken")
        void shouldUpdateProfileAndSaveWhenNewEmailIsNotTaken() {
            UUID userId = UUID.randomUUID();
            User user = User.create("johndoe", "old@example.com", "password123", "John", "Doe", UserRole.CUSTOMER);

            UpdateUserCommand command = new UpdateUserCommand(userId, "Jane", "Smith", "new@example.com");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

            userCommandService.update(command);

            assertThat(user.getFirstName()).isEqualTo("Jane");
            assertThat(user.getLastName()).isEqualTo("Smith");
            assertThat(user.getEmail()).isEqualTo("new@example.com");

            verify(userRepository).findById(userId);
            verify(userRepository).existsByEmail("new@example.com");
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should throw InvalidUserStateException when updating to an email that is already in use")
        void shouldThrowInvalidUserStateExceptionWhenUpdatingToEmailAlreadyInUse() {
            UUID userId = UUID.randomUUID();
            User user = User.create("johndoe", "old@example.com", "password123", "John", "Doe", UserRole.CUSTOMER);

            UpdateUserCommand command = new UpdateUserCommand(userId, "Jane", "Smith", "taken@example.com");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userCommandService.update(command))
                    .isInstanceOf(InvalidUserStateException.class)
                    .hasMessageContaining("This email address is already in use");

            verify(userRepository).findById(userId);
            verify(userRepository).existsByEmail("taken@example.com");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when updating user that is not found by id")
        void shouldThrowUserNotFoundExceptionWhenUpdatingUserThatIsNotFoundById() {
            UUID userId = UUID.randomUUID();
            UpdateUserCommand command = new UpdateUserCommand(userId, "Jane", "Smith", "new@example.com");

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userCommandService.update(command))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository).findById(userId);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("ChangePassword Tests")
    class ChangePassword {

        @Test
        @DisplayName("should change password and save when user is found by id and current password matches")
        void shouldChangePasswordAndSaveWhenUserIsFoundById() {
            UUID userId = UUID.randomUUID();
            User user = User.create("johndoe", "john@example.com", "oldPassword123", "John", "Doe", UserRole.CUSTOMER);

            ChangePasswordCommand command = new ChangePasswordCommand(userId, "oldPassword123", "newPassword456");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldPassword123", user.getPassword())).thenReturn(true);
            when(passwordEncoder.matches("newPassword456", user.getPassword())).thenReturn(false);
            when(passwordEncoder.encode("newPassword456")).thenReturn("encodedNewPassword456");

            userCommandService.changePassword(command);

            assertThat(user.getPassword()).isEqualTo("encodedNewPassword456");

            verify(userRepository).findById(userId);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should throw InvalidUserStateException when current password does not match")
        void shouldThrowInvalidUserStateExceptionWhenCurrentPasswordDoesNotMatch() {
            UUID userId = UUID.randomUUID();
            User user = User.create("johndoe", "john@example.com", "oldPassword123", "John", "Doe", UserRole.CUSTOMER);

            ChangePasswordCommand command = new ChangePasswordCommand(userId, "wrongPassword", "newPassword456");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongPassword", user.getPassword())).thenReturn(false);

            assertThatThrownBy(() -> userCommandService.changePassword(command))
                    .isInstanceOf(InvalidUserStateException.class)
                    .hasMessageContaining("Current password is incorrect");

            verify(userRepository).findById(userId);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw InvalidUserStateException when new password is the same as current password")
        void shouldThrowInvalidUserStateExceptionWhenNewPasswordIsSameAsCurrentPassword() {
            UUID userId = UUID.randomUUID();
            User user = User.create("johndoe", "john@example.com", "oldPassword123", "John", "Doe", UserRole.CUSTOMER);

            ChangePasswordCommand command = new ChangePasswordCommand(userId, "oldPassword123", "oldPassword123");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldPassword123", user.getPassword())).thenReturn(true);

            assertThatThrownBy(() -> userCommandService.changePassword(command))
                    .isInstanceOf(InvalidUserStateException.class)
                    .hasMessageContaining("New password cannot be the same as current password");

            verify(userRepository).findById(userId);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when changing password of user that is not found by id")
        void shouldThrowUserNotFoundExceptionWhenChangingPasswordOfUserThatIsNotFoundById() {
            UUID userId = UUID.randomUUID();
            ChangePasswordCommand command = new ChangePasswordCommand(userId, "oldPassword123", "newPassword456");

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userCommandService.changePassword(command))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository).findById(userId);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class Delete {

        @Test
        @DisplayName("should delete user when user is found by id")
        void shouldDeleteUserWhenUserIsFoundById() {
            UUID userId = UUID.randomUUID();
            User user = User.create("johndoe", "john@example.com", "password123", "John", "Doe", UserRole.CUSTOMER);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            userCommandService.delete(userId);

            verify(userRepository).findById(userId);
            verify(userRepository).delete(user.getId());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when deleting user that is not found by id")
        void shouldThrowUserNotFoundExceptionWhenDeletingUserThatIsNotFoundById() {
            UUID userId = UUID.randomUUID();

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userCommandService.delete(userId))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository).findById(userId);
            verify(userRepository, never()).delete(any(UUID.class));
        }
    }

    @Nested
    @DisplayName("Activate Tests")
    class Activate {

        @Test
        @DisplayName("should activate user and save when user is found by id and inactive")
        void shouldActivateUserAndSaveWhenUserIsFoundByIdAndInactive() {
            UUID userId = UUID.randomUUID();
            User user = User.restore(
                    userId,
                    "johndoe",
                    "john@example.com",
                    "password123",
                    "John",
                    "Doe",
                    UserRole.CUSTOMER,
                    UserStatus.INACTIVE,
                    LocalDateTime.now().minusDays(1)
            );

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            userCommandService.activate(userId);

            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);

            verify(userRepository).findById(userId);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when activating user that is not found by id")
        void shouldThrowUserNotFoundExceptionWhenActivatingUserThatIsNotFoundById() {
            UUID userId = UUID.randomUUID();

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userCommandService.activate(userId))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository).findById(userId);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw InvalidUserStateException when activating user that is already active")
        void shouldThrowInvalidUserStateExceptionWhenActivatingUserThatIsAlreadyActive() {
            UUID userId = UUID.randomUUID();
            User user = User.create("johndoe", "john@example.com", "password123", "John", "Doe", UserRole.CUSTOMER);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userCommandService.activate(userId))
                    .isInstanceOf(InvalidUserStateException.class);

            verify(userRepository).findById(userId);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Deactivate Tests")
    class Deactivate {

        @Test
        @DisplayName("should deactivate user and save when user is found by id and active")
        void shouldDeactivateUserAndSaveWhenUserIsFoundByIdAndActive() {
            UUID userId = UUID.randomUUID();
            User user = User.create("johndoe", "john@example.com", "password123", "John", "Doe", UserRole.CUSTOMER);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            userCommandService.deactivate(userId);

            assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);

            verify(userRepository).findById(userId);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when deactivating user that is not found by id")
        void shouldThrowUserNotFoundExceptionWhenDeactivatingUserThatIsNotFoundById() {
            UUID userId = UUID.randomUUID();

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userCommandService.deactivate(userId))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository).findById(userId);
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw InvalidUserStateException when deactivating user that is already inactive")
        void shouldThrowInvalidUserStateExceptionWhenDeactivatingUserThatIsAlreadyInactive() {
            UUID userId = UUID.randomUUID();
            User user = User.restore(
                    userId,
                    "johndoe",
                    "john@example.com",
                    "password123",
                    "John",
                    "Doe",
                    UserRole.CUSTOMER,
                    UserStatus.INACTIVE,
                    LocalDateTime.now().minusDays(1)
            );

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userCommandService.deactivate(userId))
                    .isInstanceOf(InvalidUserStateException.class);

            verify(userRepository).findById(userId);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Block Tests")
    class Block {

        @Test
        @DisplayName("should block user and save when user is found by id")
        void shouldBlockUserAndSaveWhenUserIsFoundById() {
            UUID userId = UUID.randomUUID();
            User user = User.create("johndoe", "john@example.com", "password123", "John", "Doe", UserRole.CUSTOMER);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            userCommandService.block(userId);

            assertThat(user.getStatus()).isEqualTo(UserStatus.BLOCKED);

            verify(userRepository).findById(userId);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when blocking user that is not found by id")
        void shouldThrowUserNotFoundExceptionWhenBlockingUserNotFound() {
            UUID userId = UUID.randomUUID();

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userCommandService.block(userId))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository).findById(userId);
            verify(userRepository, never()).save(any());
        }
    }
}
