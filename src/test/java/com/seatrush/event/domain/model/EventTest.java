package com.seatrush.event.domain.model;

import com.seatrush.event.domain.exception.InvalidEventStateException;
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

@DisplayName("Event Domain Model Unit Tests")
public class EventTest {

    private final UUID id = UUID.randomUUID();
    private final String title = "title";
    private final String description = "description";
    private final LocalDateTime startTime = LocalDateTime.of(2026, 8, 24, 4, 3);
    private final LocalDateTime endTime = LocalDateTime.of(2026, 8, 25, 4, 3);
    private final int totalCapacity = 100;

    @Nested
    @DisplayName("Creation Tests")
    class Creation {

        @Test
        @DisplayName("Should create event with DRAFT status when valid parameters provided")
        void shouldCreateDraftEventWhenValidParametersProvided() {
            Event event = Event.create(title, description, startTime, endTime, totalCapacity);

            assertThat(event.getId()).isNotNull();
            assertThat(event.getTitle()).isEqualTo(title);
            assertThat(event.getDescription()).isEqualTo(description);
            assertThat(event.getStartTime()).isEqualTo(startTime);
            assertThat(event.getEndTime()).isEqualTo(endTime);
            assertThat(event.getTotalCapacity()).isEqualTo(totalCapacity);
            assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Should throw exception when title is null or blank")
        void shouldThrowExceptionWhenTitleIsNullOrEmptyOrBlank(String blankTitle) {
            assertThatThrownBy(() -> Event.create(blankTitle, description, startTime, endTime, totalCapacity))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Should throw exception when description is null or blank")
        void shouldThrowExceptionWhenDescriptionIsBlank(String blankDescription) {
            assertThatThrownBy(() -> Event.create(title, blankDescription, startTime, endTime, totalCapacity))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when startTime is null")
        void shouldThrowExceptionWhenStartTimeIsNull() {
            assertThatThrownBy(() -> Event.create(title, description, null, endTime, totalCapacity))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when endTime is null")
        void shouldThrowExceptionWhenEndTimeIsNull() {
            assertThatThrownBy(() -> Event.create(title, description, startTime, null, totalCapacity))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when startTime is after endTime")
        void shouldThrowExceptionWhenStartTimeIsAfterEndTime() {
            assertThatThrownBy(() -> Event.create(
                    title,
                    description,
                    LocalDateTime.of(2024, 10, 10, 5, 5),
                    LocalDateTime.of(2022, 10, 10, 5, 5),
                    totalCapacity
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when startTime is equal to endTime")
        void shouldThrowExceptionWhenStartTimeEqualsEndTime() {
            LocalDateTime sameTime = LocalDateTime.of(2026, 8, 25, 4, 3);

            assertThatThrownBy(() -> Event.create(
                    title,
                    description,
                    sameTime,
                    sameTime,
                    totalCapacity
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10, Integer.MIN_VALUE})
        @DisplayName("Should throw exception when totalCapacity is zero or negative")
        void shouldThrowExceptionWhenTotalCapacityIsNotPositive(int invalidCapacity) {
            assertThatThrownBy(() -> Event.create(title, description, startTime, endTime, invalidCapacity))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Restore Tests")
    class Restore {

        @Test
        @DisplayName("Should restore event when valid state provided")
        void shouldRestoreEventWhenValidStateProvided() {
            Event event = Event.restore(
                    id,
                    title,
                    description,
                    EventStatus.ACTIVE,
                    startTime,
                    endTime,
                    totalCapacity
            );

            assertThat(event.getId()).isEqualTo(id);
            assertThat(event.getTitle()).isEqualTo(title);
            assertThat(event.getDescription()).isEqualTo(description);
            assertThat(event.getStartTime()).isEqualTo(startTime);
            assertThat(event.getEndTime()).isEqualTo(endTime);
            assertThat(event.getTotalCapacity()).isEqualTo(totalCapacity);
            assertThat(event.getStatus()).isEqualTo(EventStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should throw exception when restored with null id")
        void shouldThrowExceptionWhenRestoredWithNullId() {
            assertThatThrownBy(() -> Event.restore(
                    null,
                    title,
                    description,
                    EventStatus.DRAFT,
                    startTime,
                    endTime,
                    totalCapacity
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Should throw exception when restored with null or blank title")
        void shouldThrowExceptionWhenRestoredWithNullOrBlankTitle(String blankTitle) {
            assertThatThrownBy(() -> Event.restore(
                    id,
                    blankTitle,
                    description,
                    EventStatus.DRAFT,
                    startTime,
                    endTime,
                    totalCapacity
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Should throw exception when restored with null or blank description")
        void shouldThrowExceptionWhenRestoredWithBlankDescription(String blankDescription) {
            assertThatThrownBy(() -> Event.restore(
                    id,
                    title,
                    blankDescription,
                    EventStatus.DRAFT,
                    startTime,
                    endTime,
                    totalCapacity
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored with null status")
        void shouldThrowExceptionWhenRestoredWithNullStatus() {
            assertThatThrownBy(() -> Event.restore(
                    id,
                    title,
                    description,
                    null,
                    startTime,
                    endTime,
                    totalCapacity
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored with null startTime")
        void shouldThrowExceptionWhenRestoredWithNullStartTime() {
            assertThatThrownBy(() -> Event.restore(
                    id,
                    title,
                    description,
                    EventStatus.DRAFT,
                    null,
                    endTime,
                    totalCapacity
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored with null endTime")
        void shouldThrowExceptionWhenRestoredWithNullEndTime() {
            assertThatThrownBy(() -> Event.restore(
                    id,
                    title,
                    description,
                    EventStatus.DRAFT,
                    startTime,
                    null,
                    totalCapacity
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored with startTime after endTime")
        void shouldThrowExceptionWhenRestoredWithStartTimeAfterEndTime() {
            assertThatThrownBy(() -> Event.restore(
                    id,
                    title,
                    description,
                    EventStatus.DRAFT,
                    LocalDateTime.of(2024, 10, 10, 5, 5),
                    LocalDateTime.of(2022, 10, 10, 5, 5),
                    totalCapacity
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored with equal startTime and endTime")
        void shouldThrowExceptionWhenRestoredWithStartTimeEqualsEndTime() {
            LocalDateTime sameTime = LocalDateTime.of(2026, 8, 25, 4, 3);

            assertThatThrownBy(() -> Event.restore(
                    id,
                    title,
                    description,
                    EventStatus.DRAFT,
                    sameTime,
                    sameTime,
                    totalCapacity
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10, Integer.MIN_VALUE})
        @DisplayName("Should throw exception when restored with zero or negative totalCapacity")
        void shouldThrowExceptionWhenRestoredWithNonPositiveTotalCapacity(int invalidCapacity) {
            assertThatThrownBy(() -> Event.restore(
                    id,
                    title,
                    description,
                    EventStatus.DRAFT,
                    startTime,
                    endTime,
                    invalidCapacity
            )).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("UpdateDetails Tests")
    class UpdateDetails {

        @Test
        @DisplayName("Should update details when valid parameters provided")
        void shouldUpdateDetailsWhenValidParametersProvided() {
            Event event = Event.create(title, description, startTime, endTime, totalCapacity);

            event.updateDetails(
                    "Updated Title",
                    "Updated Description",
                    LocalDateTime.of(2026, 9, 1, 10, 0),
                    LocalDateTime.of(2026, 9, 2, 10, 0),
                    150
            );

            assertThat(event.getId()).isNotNull();
            assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);
            assertThat(event.getTitle()).isEqualTo("Updated Title");
            assertThat(event.getDescription()).isEqualTo("Updated Description");
            assertThat(event.getStartTime()).isEqualTo(LocalDateTime.of(2026, 9, 1, 10, 0));
            assertThat(event.getEndTime()).isEqualTo(LocalDateTime.of(2026, 9, 2, 10, 0));
            assertThat(event.getTotalCapacity()).isEqualTo(150);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Should throw exception when updating with null or blank title")
        void shouldThrowExceptionWhenUpdatingWithNullOrBlankTitle(String blankTitle) {
            Event event = Event.create(title, description, startTime, endTime, totalCapacity);

            assertThatThrownBy(() -> event.updateDetails(
                    blankTitle,
                    "Updated Description",
                    LocalDateTime.of(2026, 9, 1, 10, 0),
                    LocalDateTime.of(2026, 9, 2, 10, 0),
                    150
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Should throw exception when updating with blank description")
        void shouldThrowExceptionWhenUpdatingWithBlankDescription(String blankDescription) {
            Event event = Event.create(title, description, startTime, endTime, totalCapacity);

            assertThatThrownBy(() -> event.updateDetails(
                    title,
                    blankDescription,
                    LocalDateTime.of(2026, 9, 1, 10, 0),
                    LocalDateTime.of(2026, 9, 2, 10, 0),
                    150
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when updating with null startTime")
        void shouldThrowExceptionWhenUpdatingWithNullStartTime() {
            Event event = Event.create(title, description, startTime, endTime, totalCapacity);

            assertThatThrownBy(() -> event.updateDetails(
                    title,
                    description,
                    null,
                    LocalDateTime.of(2026, 9, 2, 10, 0),
                    150
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when updating with null endTime")
        void shouldThrowExceptionWhenUpdatingWithNullEndTime() {
            Event event = Event.create(title, description, startTime, endTime, totalCapacity);

            assertThatThrownBy(() -> event.updateDetails(
                    title,
                    description,
                    LocalDateTime.of(2026, 9, 1, 10, 0),
                    null,
                    150
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when updating with startTime after endTime")
        void shouldThrowExceptionWhenUpdatingWithStartTimeAfterEndTime() {
            Event event = Event.create(title, description, startTime, endTime, totalCapacity);

            assertThatThrownBy(() -> event.updateDetails(
                    title,
                    description,
                    LocalDateTime.of(2026, 9, 5, 10, 0),
                    LocalDateTime.of(2026, 9, 1, 10, 0),
                    150
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when updating with equal startTime and endTime")
        void shouldThrowExceptionWhenUpdatingWithStartTimeEqualsEndTime() {
            Event event = Event.create(title, description, startTime, endTime, totalCapacity);
            LocalDateTime sameTime = LocalDateTime.of(2026, 9, 1, 10, 0);

            assertThatThrownBy(() -> event.updateDetails(
                    title,
                    description,
                    sameTime,
                    sameTime,
                    150
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10, Integer.MIN_VALUE})
        @DisplayName("Should throw exception when updating with zero or negative totalCapacity")
        void shouldThrowExceptionWhenUpdatingWithNonPositiveTotalCapacity(int invalidCapacity) {
            Event event = Event.create(title, description, startTime, endTime, totalCapacity);

            assertThatThrownBy(() -> event.updateDetails(
                    title,
                    description,
                    LocalDateTime.of(2026, 9, 1, 10, 0),
                    LocalDateTime.of(2026, 9, 2, 10, 0),
                    invalidCapacity
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when updating details of a cancelled event")
        void shouldThrowExceptionWhenUpdatingCancelledEvent() {
            Event event = Event.create(title, description, startTime, endTime, totalCapacity);
            event.cancel();

            assertThatThrownBy(() -> event.updateDetails(
                    "New Title",
                    "New Desc",
                    LocalDateTime.of(2026, 9, 1, 10, 0),
                    LocalDateTime.of(2026, 9, 2, 10, 0),
                    100
            )).isInstanceOf(InvalidEventStateException.class);
        }
    }

    @Nested
    @DisplayName("Activate Tests")
    class Activate {

        @Test
        @DisplayName("Should transition to ACTIVE status when event is DRAFT")
        void shouldTransitionToActiveWhenEventIsDraft() {
            Event event = eventWithStatus(EventStatus.DRAFT);

            event.activate();

            assertThat(event.getStatus()).isEqualTo(EventStatus.ACTIVE);
        }

        @ParameterizedTest
        @EnumSource(value = EventStatus.class, names = {"ACTIVE", "CANCELLED"})
        @DisplayName("Should throw InvalidEventStateException when activating a non-draft event")
        void shouldThrowInvalidEventStateExceptionWhenActivatingNonDraftEvent(EventStatus eventStatus) {
            Event event = eventWithStatus(eventStatus);

            assertThatThrownBy(event::activate)
                    .isInstanceOf(InvalidEventStateException.class);
        }
    }

    @Nested
    @DisplayName("Cancel Tests")
    class Cancel {

        @ParameterizedTest
        @EnumSource(value = EventStatus.class, names = {"ACTIVE", "DRAFT"})
        @DisplayName("Should transition to CANCELLED status when event is DRAFT or ACTIVE")
        void shouldTransitionToCancelledWhenEventIsDraftOrActive(EventStatus eventStatus) {
            Event event = eventWithStatus(eventStatus);

            event.cancel();

            assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should throw InvalidEventStateException when cancelling an already cancelled event")
        void shouldThrowInvalidEventStateExceptionWhenCancellingAlreadyCancelledEvent() {
            Event event = eventWithStatus(EventStatus.CANCELLED);

            assertThatThrownBy(event::cancel)
                    .isInstanceOf(InvalidEventStateException.class);
        }
    }

    private Event eventWithStatus(EventStatus status) {
        return Event.restore(
                id,
                title,
                description,
                status,
                startTime,
                endTime,
                totalCapacity
        );
    }
}
