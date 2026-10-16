package com.seatrush.event.application.service;

import com.seatrush.event.application.port.out.EventQueryPort;
import com.seatrush.event.application.query.EventView;
import com.seatrush.event.domain.exception.EventNotFoundException;
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
@DisplayName("EventQueryService Unit Tests")
public class EventQueryServiceTest {

    @Mock
    private EventQueryPort eventQueryPort;

    @InjectMocks
    private EventQueryService eventQueryService;

    private static final String title = "title";
    private static final String desc = "desc";
    private static final LocalDateTime startTime = LocalDateTime.of(2026, 11, 15, 23, 23);
    private static final LocalDateTime endTime = LocalDateTime.of(2026, 11, 17, 23, 23);
    private static final int totalCapacity = 15;
    private static final UUID eventId = UUID.randomUUID();

    @Nested
    @DisplayName("GetById Tests")
    class GetById {

        @Test
        @DisplayName("should return EventView when event is found by id")
        void shouldReturnEventViewWhenEventIsFoundById() {
            EventView expectedView = new EventView(title, desc, startTime, endTime, totalCapacity);

            when(eventQueryPort.findById(eventId)).thenReturn(Optional.of(expectedView));

            EventView actualView = eventQueryService.getById(eventId);

            assertThat(actualView).isEqualTo(expectedView);
            verify(eventQueryPort).findById(eventId);
        }

        @Test
        @DisplayName("should throw EventNotFoundException when event is not found by id")
        void shouldThrowEventNotFoundExceptionWhenEventIsNotFoundById() {
            UUID id = UUID.randomUUID();

            when(eventQueryPort.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventQueryService.getById(id))
                    .isInstanceOf(EventNotFoundException.class);

            verify(eventQueryPort).findById(id);
        }
    }

    @Nested
    @DisplayName("GetByTitle Tests")
    class GetByTitle {

        @Test
        @DisplayName("should return EventView when event is found by title")
        void shouldReturnEventViewWhenEventIsFoundByTitle() {
            EventView expectedView = new EventView(title, desc, startTime, endTime, totalCapacity);

            when(eventQueryPort.findByTitle(title)).thenReturn(Optional.of(expectedView));

            EventView actualView = eventQueryService.getByTitle(title);

            assertThat(actualView).isEqualTo(expectedView);
            verify(eventQueryPort).findByTitle(title);
        }

        @Test
        @DisplayName("should throw EventNotFoundException when event is not found by title")
        void shouldThrowEventNotFoundExceptionWhenEventIsNotFoundByTitle() {
            String testTitle = "nonExistingTitle";

            when(eventQueryPort.findByTitle(testTitle)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventQueryService.getByTitle(testTitle))
                    .isInstanceOf(EventNotFoundException.class);

            verify(eventQueryPort).findByTitle(testTitle);
        }
    }

    @Nested
    @DisplayName("GetAll Tests")
    class GetAll {

        @Test
        @DisplayName("should return all event views")
        void shouldReturnAllEventViews() {
            List<EventView> expectedViews = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                expectedViews.add(new EventView("title" + i, desc, startTime, endTime, totalCapacity + i));
            }

            when(eventQueryPort.findAll()).thenReturn(expectedViews);

            List<EventView> actualViews = eventQueryService.getAll();

            assertThat(actualViews).containsExactlyElementsOf(expectedViews);
            verify(eventQueryPort).findAll();
        }

        @Test
        @DisplayName("should return empty list when no events exist")
        void shouldReturnEmptyListWhenNoEventsExist() {
            when(eventQueryPort.findAll()).thenReturn(List.of());

            List<EventView> actualViews = eventQueryService.getAll();

            assertThat(actualViews).isEmpty();
            verify(eventQueryPort).findAll();
        }
    }
}
