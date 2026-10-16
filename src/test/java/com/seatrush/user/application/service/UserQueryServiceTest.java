package com.seatrush.user.application.service;

import com.seatrush.user.application.port.out.UserQueryPort;
import com.seatrush.user.application.query.UserView;
import com.seatrush.user.domain.exception.UserNotFoundException;
import com.seatrush.user.domain.model.UserRole;
import com.seatrush.user.domain.model.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserQueryService Unit Tests")
class UserQueryServiceTest {

    @Mock
    private UserQueryPort userQueryPort;

    @InjectMocks
    private UserQueryService userQueryService;

    @Nested
    @DisplayName("GetById Tests")
    class GetById {

        @Test
        @DisplayName("should return user view when user is found by id")
        void shouldReturnUserViewWhenUserIsFoundById() {
            UUID userId = UUID.randomUUID();
            UserView expectedView = new UserView(
                    userId,
                    "johndoe",
                    "john@example.com",
                    "John",
                    "Doe",
                    UserRole.CUSTOMER,
                    UserStatus.ACTIVE,
                    LocalDateTime.now()
            );

            when(userQueryPort.findById(userId)).thenReturn(Optional.of(expectedView));

            UserView actualView = userQueryService.getById(userId);

            assertThat(actualView).isEqualTo(expectedView);
            verify(userQueryPort).findById(userId);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user is not found by id")
        void shouldThrowUserNotFoundExceptionWhenUserIsNotFoundById() {
            UUID userId = UUID.randomUUID();

            when(userQueryPort.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userQueryService.getById(userId))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userQueryPort).findById(userId);
        }
    }

    @Nested
    @DisplayName("GetByEmail Tests")
    class GetByEmail {

        @Test
        @DisplayName("should return user view when user is found by email")
        void shouldReturnUserViewWhenUserIsFoundByEmail() {
            String email = "john@example.com";
            UserView expectedView = new UserView(
                    UUID.randomUUID(),
                    "johndoe",
                    email,
                    "John",
                    "Doe",
                    UserRole.CUSTOMER,
                    UserStatus.ACTIVE,
                    LocalDateTime.now()
            );

            when(userQueryPort.findByEmail(email)).thenReturn(Optional.of(expectedView));

            UserView actualView = userQueryService.getByEmail(email);

            assertThat(actualView).isEqualTo(expectedView);
            verify(userQueryPort).findByEmail(email);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user is not found by email")
        void shouldThrowUserNotFoundExceptionWhenUserIsNotFoundByEmail() {
            String email = "nonexisting@example.com";

            when(userQueryPort.findByEmail(email)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userQueryService.getByEmail(email))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userQueryPort).findByEmail(email);
        }
    }

    @Nested
    @DisplayName("GetByUsername Tests")
    class GetByUsername {

        @Test
        @DisplayName("should return user view when user is found by username")
        void shouldReturnUserViewWhenUserIsFoundByUsername() {
            String username = "johndoe";
            UserView expectedView = new UserView(
                    UUID.randomUUID(),
                    username,
                    "john@example.com",
                    "John",
                    "Doe",
                    UserRole.CUSTOMER,
                    UserStatus.ACTIVE,
                    LocalDateTime.now()
            );

            when(userQueryPort.findByUsername(username)).thenReturn(Optional.of(expectedView));

            UserView actualView = userQueryService.getByUsername(username);

            assertThat(actualView).isEqualTo(expectedView);
            verify(userQueryPort).findByUsername(username);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user is not found by username")
        void shouldThrowUserNotFoundExceptionWhenUserIsNotFoundByUsername() {
            String username = "nonexistinguser";

            when(userQueryPort.findByUsername(username)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userQueryService.getByUsername(username))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userQueryPort).findByUsername(username);
        }
    }

    @Nested
    @DisplayName("GetAll Tests")
    class GetAll {

        @Test
        @DisplayName("should return all user views")
        void shouldReturnAllUserViews() {
            List<UserView> expectedViews = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                expectedViews.add(new UserView(
                        UUID.randomUUID(),
                        "user" + i,
                        "user" + i + "@example.com",
                        "First" + i,
                        "Last" + i,
                        UserRole.CUSTOMER,
                        UserStatus.ACTIVE,
                        LocalDateTime.now()
                ));
            }

            when(userQueryPort.findAll()).thenReturn(expectedViews);

            List<UserView> actualViews = userQueryService.getAll();

            assertThat(actualViews).containsExactlyElementsOf(expectedViews);
            verify(userQueryPort).findAll();
        }

        @Test
        @DisplayName("should return empty list when no users exist")
        void shouldReturnEmptyListWhenNoUsersExist() {
            when(userQueryPort.findAll()).thenReturn(List.of());

            List<UserView> actualViews = userQueryService.getAll();

            assertThat(actualViews).isEmpty();
            verify(userQueryPort).findAll();
        }
    }
}
