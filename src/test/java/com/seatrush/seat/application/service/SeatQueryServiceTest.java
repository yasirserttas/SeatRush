package com.seatrush.seat.application.service;

import com.seatrush.seat.application.port.out.SeatQueryPort;
import com.seatrush.seat.application.query.SeatView;
import com.seatrush.seat.domain.exception.SeatNotFoundException;
import com.seatrush.seat.domain.model.SeatStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeatQueryService Unit Tests")
public class SeatQueryServiceTest {

    @Mock
    private SeatQueryPort seatQueryPort;

    @InjectMocks
    private SeatQueryService seatQueryService;

    @Nested
    @DisplayName("GetById Tests")
    class GetById {

        @Test
        @DisplayName("should return seat view when seat is found by id")
        void shouldReturnSeatViewWhenSeatIsFoundById() {
            UUID seatId = UUID.randomUUID();
            UUID eventId = UUID.randomUUID();
            SeatView expectedView = new SeatView(seatId, eventId, 1, SeatStatus.AVAILABLE);

            when(seatQueryPort.findViewBySeatId(seatId)).thenReturn(Optional.of(expectedView));

            SeatView actualView = seatQueryService.getById(seatId);

            assertThat(actualView).isEqualTo(expectedView);
            verify(seatQueryPort).findViewBySeatId(seatId);
        }

        @Test
        @DisplayName("should throw SeatNotFoundException when seat is not found by id")
        void shouldThrowSeatNotFoundExceptionWhenSeatIsNotFoundById() {
            UUID seatId = UUID.randomUUID();

            when(seatQueryPort.findViewBySeatId(seatId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> seatQueryService.getById(seatId))
                    .isInstanceOf(SeatNotFoundException.class);

            verify(seatQueryPort).findViewBySeatId(seatId);
        }
    }

    @Nested
    @DisplayName("GetByIdAndEventId Tests")
    class GetByIdAndEventId {

        @Test
        @DisplayName("should return seat view when seat is found by seat id and event id")
        void shouldReturnSeatViewWhenSeatIsFoundBySeatIdAndEventId() {
            UUID seatId = UUID.randomUUID();
            UUID eventId = UUID.randomUUID();

            SeatView expectedView = new SeatView(seatId, eventId, 1, SeatStatus.RESERVED);

            when(seatQueryPort.findViewByIdAndEventId(seatId, eventId)).thenReturn(Optional.of(expectedView));

            SeatView actualView = seatQueryService.getByIdAndEventId(seatId, eventId);

            assertThat(actualView).isEqualTo(expectedView);
            verify(seatQueryPort).findViewByIdAndEventId(seatId, eventId);
        }

        @Test
        @DisplayName("should throw SeatNotFoundException when seat is not found by seat id and event id")
        void shouldThrowSeatNotFoundExceptionWhenSeatIsNotFoundBySeatIdAndEventId() {
            UUID seatId = UUID.randomUUID();
            UUID eventId = UUID.randomUUID();

            when(seatQueryPort.findViewByIdAndEventId(seatId, eventId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> seatQueryService.getByIdAndEventId(seatId, eventId))
                    .isInstanceOf(SeatNotFoundException.class);

            verify(seatQueryPort).findViewByIdAndEventId(seatId, eventId);
        }
    }

    @Nested
    @DisplayName("GetByEventId Tests")
    class GetByEventId {

        @Test
        @DisplayName("should return seat view list when seats are found by event id")
        void shouldReturnSeatViewListWhenSeatsAreFoundByEventId() {
            UUID eventId = UUID.randomUUID();
            List<SeatView> expectedViews = new ArrayList<>();

            for (int i = 1; i <= 3; i++) {
                SeatView seatView = new SeatView(UUID.randomUUID(), eventId, i, SeatStatus.RESERVED);
                expectedViews.add(seatView);
            }

            when(seatQueryPort.findByEventId(eventId)).thenReturn(expectedViews);

            List<SeatView> actualViews = seatQueryService.getByEventId(eventId);

            assertThat(actualViews).containsExactlyInAnyOrderElementsOf(expectedViews);
            verify(seatQueryPort).findByEventId(eventId);
        }

        @Test
        @DisplayName("should return empty list when no seats found by event id")
        void shouldReturnEmptyListWhenNoSeatsFoundByEventId() {
            UUID eventId = UUID.randomUUID();

            when(seatQueryPort.findByEventId(eventId)).thenReturn(List.of());

            List<SeatView> actualViews = seatQueryService.getByEventId(eventId);

            assertThat(actualViews).isEmpty();
            verify(seatQueryPort).findByEventId(eventId);
        }
    }

    @Nested
    @DisplayName("GetAll Tests")
    class GetAll {

        @Test
        @DisplayName("should return all seat views")
        void shouldReturnAllSeatViews() {
            List<SeatView> expectedViews = new ArrayList<>();
            UUID eventId = UUID.randomUUID();

            for (int i = 1; i <= 3; i++) {
                expectedViews.add(
                        new SeatView(
                                UUID.randomUUID(),
                                eventId,
                                i,
                                SeatStatus.AVAILABLE
                        )
                );
            }

            when(seatQueryPort.findAll()).thenReturn(expectedViews);

            List<SeatView> actualViews = seatQueryService.getAll();

            assertThat(actualViews).containsExactlyElementsOf(expectedViews);
            verify(seatQueryPort).findAll();
        }

        @Test
        @DisplayName("should return empty list when no seats exist")
        void shouldReturnEmptyListWhenNoSeatsExist() {
            when(seatQueryPort.findAll()).thenReturn(List.of());

            List<SeatView> actualViews = seatQueryService.getAll();

            assertThat(actualViews).isEmpty();
            verify(seatQueryPort).findAll();
        }
    }
}

