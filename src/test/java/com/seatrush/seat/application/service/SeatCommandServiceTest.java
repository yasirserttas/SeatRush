package com.seatrush.seat.application.service;

import com.seatrush.seat.application.command.CreateSeatCommand;
import com.seatrush.seat.application.command.CreateSeatsForEventCommand;
import com.seatrush.seat.application.port.out.SeatRepository;
import com.seatrush.seat.domain.exception.InvalidSeatStateException;
import com.seatrush.seat.domain.exception.SeatNotFoundException;
import com.seatrush.seat.domain.model.Seat;
import com.seatrush.seat.domain.model.SeatStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeatCommandService Unit Tests")
class SeatCommandServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private SeatCommandService seatCommandService;

    @Nested
    @DisplayName("Create Tests")
    class Create {

        @Captor
        private ArgumentCaptor<List<Seat>> seatListCaptor;

        @Test
        @DisplayName("should create, save seat and return seat id when command is valid")
        void shouldCreateSaveSeatAndReturnSeatIdWhenCommandIsValid() {
            UUID eventId = UUID.randomUUID();
            int seatNumber = 1;

            CreateSeatCommand command = new CreateSeatCommand(eventId, seatNumber);
            ArgumentCaptor<Seat> seatCaptor = ArgumentCaptor.forClass(Seat.class);

            UUID actualSeatId = seatCommandService.create(command);

            verify(seatRepository).save(seatCaptor.capture());

            Seat savedSeat = seatCaptor.getValue();

            assertThat(savedSeat.getEventId()).isEqualTo(eventId);
            assertThat(savedSeat.getSeatNumber()).isEqualTo(seatNumber);
            assertThat(savedSeat.getSeatStatus()).isEqualTo(SeatStatus.AVAILABLE);
            assertThat(actualSeatId).isEqualTo(savedSeat.getId());
        }

        @Test
        @DisplayName("should create seats for event when command is valid")
        void shouldCreateSeatsForEventWhenCommandIsValid() {
            UUID eventId = UUID.randomUUID();
            int capacity = 3;

            CreateSeatsForEventCommand command = new CreateSeatsForEventCommand(eventId, capacity);

            seatCommandService.createSeatsForEvent(command);

            verify(seatRepository).saveAll(seatListCaptor.capture());

            List<Seat> capturedSeats = seatListCaptor.getValue();

            assertThat(capturedSeats).extracting(Seat::getSeatNumber)
                    .containsExactly(1, 2, 3);

            assertThat(capturedSeats).extracting(Seat::getEventId)
                    .containsOnly(eventId);
        }

        @Test
        @DisplayName("should create exactly one seat numbered 1 when capacity is 1 (off-by-one boundary)")
        void shouldCreateExactlyOneSeatNumberedOneWhenCapacityIsOne() {
            UUID eventId = UUID.randomUUID();

            seatCommandService.createSeatsForEvent(new CreateSeatsForEventCommand(eventId, 1));

            verify(seatRepository).saveAll(seatListCaptor.capture());

            List<Seat> capturedSeats = seatListCaptor.getValue();

            assertThat(capturedSeats).hasSize(1);
            assertThat(capturedSeats.get(0).getSeatNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("should create seats with unique ids and AVAILABLE status")
        void shouldCreateSeatsWithUniqueIdsAndAvailableStatus() {
            UUID eventId = UUID.randomUUID();

            seatCommandService.createSeatsForEvent(new CreateSeatsForEventCommand(eventId, 5));

            verify(seatRepository).saveAll(seatListCaptor.capture());

            List<Seat> capturedSeats = seatListCaptor.getValue();

            assertThat(capturedSeats).extracting(Seat::getId).doesNotHaveDuplicates();
            assertThat(capturedSeats).extracting(Seat::getSeatStatus)
                    .containsOnly(SeatStatus.AVAILABLE);
        }
    }

    @Nested
    @DisplayName("Release Tests")
    class Release {

        @Test
        @DisplayName("should release seat when seat is reserved")
        void shouldReleaseSeatWhenSeatIsReserved() {
            UUID eventId = UUID.randomUUID();
            UUID seatId = UUID.randomUUID();

            Seat seat = Seat.restore(seatId, eventId, 1, SeatStatus.RESERVED);

            when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

            seatCommandService.release(seatId);

            assertThat(seat.getSeatStatus()).isEqualTo(SeatStatus.AVAILABLE);

            verify(seatRepository).findById(seatId);
            verify(seatRepository).save(seat);
        }

        @Test
        @DisplayName("should throw SeatNotFoundException when releasing seat that is not found by id")
        void shouldThrowSeatNotFoundExceptionWhenReleasingSeatThatIsNotFound() {
            UUID seatId = UUID.randomUUID();

            when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> seatCommandService.release(seatId))
                    .isInstanceOf(SeatNotFoundException.class);

            verify(seatRepository).findById(seatId);
            verify(seatRepository, never()).save(any(Seat.class));
        }

        @Test
        @DisplayName("should release seat when seat is sold")
        void shouldReleaseSeatWhenSeatIsSold() {
            UUID eventId = UUID.randomUUID();
            UUID seatId = UUID.randomUUID();

            Seat seat = Seat.restore(seatId, eventId, 1, SeatStatus.SOLD);

            when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

            seatCommandService.release(seatId);

            assertThat(seat.getSeatStatus()).isEqualTo(SeatStatus.AVAILABLE);

            verify(seatRepository).findById(seatId);
            verify(seatRepository).save(seat);
        }

        @Test
        @DisplayName("should throw InvalidSeatStateException when releasing seat that status is available")
        void shouldThrowInvalidSeatStateExceptionWhenReleasingSeatThatStatusIsAvailable() {
            UUID eventId = UUID.randomUUID();
            UUID seatId = UUID.randomUUID();

            Seat seat = Seat.restore(seatId, eventId, 1, SeatStatus.AVAILABLE);

            when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

            assertThatThrownBy(() -> seatCommandService.release(seatId))
                    .isInstanceOf(InvalidSeatStateException.class);

            verify(seatRepository).findById(seatId);
            verify(seatRepository, never()).save(any(Seat.class));
        }
    }

    @Nested
    @DisplayName("Reserve Tests")
    class Reserve {

        @Test
        @DisplayName("should reserve and save seat when seat is available")
        void shouldReserveAndSaveSeatWhenSeatIsAvailable() {
            UUID eventId = UUID.randomUUID();
            UUID seatId = UUID.randomUUID();

            Seat seat = Seat.restore(seatId, eventId, 1, SeatStatus.AVAILABLE);

            when(seatRepository.findByIdWithLock(seatId)).thenReturn(Optional.of(seat));

            seatCommandService.reserve(seatId);

            assertThat(seat.getSeatStatus()).isEqualTo(SeatStatus.RESERVED);

            verify(seatRepository).findByIdWithLock(seatId);
            verify(seatRepository).save(seat);
        }

        @Test
        @DisplayName("should throw SeatNotFoundException when reserving seat that is not found by id")
        void shouldThrowSeatNotFoundExceptionWhenReservingSeatThatIsNotFound() {
            UUID seatId = UUID.randomUUID();

            when(seatRepository.findByIdWithLock(seatId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> seatCommandService.reserve(seatId))
                    .isInstanceOf(SeatNotFoundException.class);

            verify(seatRepository).findByIdWithLock(seatId);
            verify(seatRepository, never()).save(any(Seat.class));
        }

        @ParameterizedTest
        @EnumSource(value = SeatStatus.class,
                names = {"AVAILABLE"},
                mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("should throw InvalidSeatStateException when reserving seat that status is not available")
        void shouldThrowInvalidSeatStateExceptionWhenReservingSeatThatStatusIsNotAvailable(SeatStatus seatStatus) {
            UUID eventId = UUID.randomUUID();
            UUID seatId = UUID.randomUUID();

            Seat seat = Seat.restore(seatId, eventId, 1, seatStatus);

            when(seatRepository.findByIdWithLock(seatId)).thenReturn(Optional.of(seat));

            assertThatThrownBy(() -> seatCommandService.reserve(seatId))
                    .isInstanceOf(InvalidSeatStateException.class);

            verify(seatRepository).findByIdWithLock(seatId);
            verify(seatRepository, never()).save(any(Seat.class));
        }
    }

    @Nested
    @DisplayName("Sell Tests")
    class Sell {

        @Test
        @DisplayName("should sell and save seat when seat is reserved")
        void shouldSellAndSaveSeatWhenSeatIsReserved() {
            UUID eventId = UUID.randomUUID();
            UUID seatId = UUID.randomUUID();

            Seat seat = Seat.restore(seatId, eventId, 1, SeatStatus.RESERVED);

            when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

            seatCommandService.sell(seatId);

            assertThat(seat.getSeatStatus()).isEqualTo(SeatStatus.SOLD);

            verify(seatRepository).findById(seatId);
            verify(seatRepository).save(seat);
        }

        @Test
        @DisplayName("should throw SeatNotFoundException when selling seat that is not found by id")
        void shouldThrowSeatNotFoundExceptionWhenSellingSeatThatIsNotFound() {
            UUID seatId = UUID.randomUUID();

            when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> seatCommandService.sell(seatId))
                    .isInstanceOf(SeatNotFoundException.class);

            verify(seatRepository).findById(seatId);
            verify(seatRepository, never()).save(any(Seat.class));
        }

        @Test
        @DisplayName("should sell and save seat when seat is available")
        void shouldSellAndSaveSeatWhenSeatIsAvailable() {
            UUID eventId = UUID.randomUUID();
            UUID seatId = UUID.randomUUID();

            Seat seat = Seat.restore(seatId, eventId, 1, SeatStatus.AVAILABLE);

            when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

            seatCommandService.sell(seatId);

            assertThat(seat.getSeatStatus()).isEqualTo(SeatStatus.SOLD);

            verify(seatRepository).findById(seatId);
            verify(seatRepository).save(seat);
        }

        @Test
        @DisplayName("should throw InvalidSeatStateException when selling seat that is already sold")
        void shouldThrowInvalidSeatStateExceptionWhenSellingSeatThatIsAlreadySold() {
            UUID eventId = UUID.randomUUID();
            UUID seatId = UUID.randomUUID();

            Seat seat = Seat.restore(seatId, eventId, 1, SeatStatus.SOLD);

            when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));

            assertThatThrownBy(() -> seatCommandService.sell(seatId))
                    .isInstanceOf(InvalidSeatStateException.class);

            verify(seatRepository).findById(seatId);
            verify(seatRepository, never()).save(any(Seat.class));
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class Delete {

        @Test
        @DisplayName("should delete seat when seat is found by id")
        void shouldDeleteSeatWhenSeatIsFoundById() {
            Seat seat = Seat.restore(UUID.randomUUID(), UUID.randomUUID(), 1, SeatStatus.RESERVED);

            when(seatRepository.findById(seat.getId())).thenReturn(Optional.of(seat));

            seatCommandService.delete(seat.getId());

            verify(seatRepository).findById(seat.getId());
            verify(seatRepository).delete(seat.getId());
        }

        @Test
        @DisplayName("should throw SeatNotFoundException when deleting seat that is not found by id")
        void shouldThrowSeatNotFoundExceptionWhenDeletingSeatThatIsNotFoundById() {
            UUID seatId = UUID.randomUUID();

            when(seatRepository.findById(seatId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> seatCommandService.delete(seatId))
                    .isInstanceOf(SeatNotFoundException.class);

            verify(seatRepository).findById(seatId);
            verify(seatRepository, never()).delete(any(UUID.class));
        }
    }
}
