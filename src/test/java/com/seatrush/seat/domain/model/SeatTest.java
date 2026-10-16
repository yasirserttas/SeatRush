package com.seatrush.seat.domain.model;

import com.seatrush.seat.domain.exception.InvalidSeatStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Seat Domain Model Unit Tests")
public class SeatTest {

    private final UUID id = UUID.randomUUID();
    private final UUID eventId = UUID.randomUUID();
    private final int validSeatNumber = 10;

    @Nested
    @DisplayName("Creation Tests")
    class Creation {

        @Test
        @DisplayName("Should create seat with AVAILABLE status when valid parameters provided")
        void shouldCreateAvailableSeatWhenValidParametersProvided() {
            Seat seat = Seat.create(eventId, validSeatNumber);

            assertThat(seat.getId()).isNotNull();
            assertThat(seat.getEventId()).isEqualTo(eventId);
            assertThat(seat.getSeatNumber()).isEqualTo(validSeatNumber);
            assertThat(seat.getSeatStatus()).isEqualTo(SeatStatus.AVAILABLE);
            assertThat(seat.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when eventId is null")
        void shouldThrowExceptionWhenEventIdIsNull() {
            assertThatThrownBy(() -> Seat.create(null, validSeatNumber))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10, Integer.MIN_VALUE})
        @DisplayName("Should throw exception when seatNumber is zero or negative")
        void shouldThrowExceptionWhenSeatNumberIsNotPositive(int invalidSeatNumber) {
            assertThatThrownBy(() -> Seat.create(eventId, invalidSeatNumber))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Restore Tests")
    class Restore {

        @Test
        @DisplayName("Should restore seat when valid state provided")
        void shouldRestoreSeatWhenValidStateProvided() {
            Seat seat = Seat.restore(
                    id,
                    eventId,
                    validSeatNumber,
                    SeatStatus.AVAILABLE
            );

            assertThat(seat.getId()).isEqualTo(id);
            assertThat(seat.getEventId()).isEqualTo(eventId);
            assertThat(seat.getSeatNumber()).isEqualTo(validSeatNumber);
            assertThat(seat.getSeatStatus()).isEqualTo(SeatStatus.AVAILABLE);
        }

        @Test
        @DisplayName("Should throw exception when restored with null id")
        void shouldThrowExceptionWhenRestoredWithNullId() {
            assertThatThrownBy(() -> Seat.restore(
                    null,
                    eventId,
                    validSeatNumber,
                    SeatStatus.AVAILABLE
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored with null eventId")
        void shouldThrowExceptionWhenRestoredWithNullEventId() {
            assertThatThrownBy(() -> Seat.restore(
                    id,
                    null,
                    validSeatNumber,
                    SeatStatus.AVAILABLE
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10, Integer.MIN_VALUE})
        @DisplayName("Should throw exception when restored with non-positive seatNumber")
        void shouldThrowExceptionWhenRestoredWithNonPositiveSeatNumber(int invalidSeatNumber) {
            assertThatThrownBy(() -> Seat.restore(id, eventId, invalidSeatNumber, SeatStatus.AVAILABLE))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored with null status")
        void shouldThrowExceptionWhenRestoredWithNullStatus() {
            assertThatThrownBy(() -> Seat.restore(id, eventId, validSeatNumber, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Reserve Tests")
    class Reserve {

        @Test
        @DisplayName("Should transition to RESERVED status when seat is available")
        void shouldTransitionToReservedWhenSeatIsAvailable() {
            Seat seat = seatWithStatus(SeatStatus.AVAILABLE);

            seat.reserve();

            assertThat(seat.getSeatStatus()).isEqualTo(SeatStatus.RESERVED);
            assertThat(seat.isAvailable()).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = SeatStatus.class, names = {"RESERVED", "SOLD"})
        @DisplayName("Should throw InvalidSeatStateException when reserving a non-available seat")
        void shouldThrowInvalidSeatStateExceptionWhenReservingNonAvailableSeat(SeatStatus seatStatus) {
            Seat seat = seatWithStatus(seatStatus);

            assertThatThrownBy(seat::reserve)
                    .isInstanceOf(InvalidSeatStateException.class);
            assertThat(seat.getSeatStatus()).isEqualTo(seatStatus);
        }
    }

    @Nested
    @DisplayName("Sell Tests")
    class Sell {

        @Test
        @DisplayName("Should transition to SOLD status when seat is reserved")
        void shouldTransitionToSoldWhenSeatIsReserved() {
            Seat seat = seatWithStatus(SeatStatus.RESERVED);

            seat.sell();

            assertThat(seat.getSeatStatus()).isEqualTo(SeatStatus.SOLD);
            assertThat(seat.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("Should transition to SOLD status when seat is available")
        void shouldTransitionToSoldWhenSeatIsAvailable() {
            Seat seat = seatWithStatus(SeatStatus.AVAILABLE);

            seat.sell();

            assertThat(seat.getSeatStatus()).isEqualTo(SeatStatus.SOLD);
            assertThat(seat.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("Should throw InvalidSeatStateException when selling an already sold seat")
        void shouldThrowInvalidSeatStateExceptionWhenSellingAlreadySoldSeat() {
            Seat seat = seatWithStatus(SeatStatus.SOLD);

            assertThatThrownBy(seat::sell)
                    .isInstanceOf(InvalidSeatStateException.class);
            assertThat(seat.getSeatStatus()).isEqualTo(SeatStatus.SOLD);
        }
    }

    @Nested
    @DisplayName("Release Tests")
    class Release {

        @Test
        @DisplayName("Should transition to AVAILABLE status when seat is reserved")
        void shouldTransitionToAvailableWhenSeatIsReserved() {
            Seat seat = seatWithStatus(SeatStatus.RESERVED);

            seat.release();

            assertThat(seat.getSeatStatus()).isEqualTo(SeatStatus.AVAILABLE);
            assertThat(seat.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("Should transition to AVAILABLE status when seat is sold")
        void shouldTransitionToAvailableWhenSeatIsSold() {
            Seat seat = seatWithStatus(SeatStatus.SOLD);

            seat.release();

            assertThat(seat.getSeatStatus()).isEqualTo(SeatStatus.AVAILABLE);
            assertThat(seat.isAvailable()).isTrue();
        }

        @Test
        @DisplayName("Should throw InvalidSeatStateException when releasing an available seat")
        void shouldThrowInvalidSeatStateExceptionWhenReleasingAvailableSeat() {
            Seat seat = seatWithStatus(SeatStatus.AVAILABLE);

            assertThatThrownBy(seat::release)
                    .isInstanceOf(InvalidSeatStateException.class);
            assertThat(seat.getSeatStatus()).isEqualTo(SeatStatus.AVAILABLE);
        }
    }

    @Nested
    @DisplayName("Lifecycle Tests")
    class Lifecycle {

        @Test
        @DisplayName("Should allow a full reservation cycle: AVAILABLE -> RESERVED -> SOLD -> AVAILABLE -> RESERVED")
        void shouldAllowFullReservationCycle() {
            Seat seat = seatWithStatus(SeatStatus.AVAILABLE);

            seat.reserve();
            seat.sell();
            assertThat(seat.getSeatStatus()).isEqualTo(SeatStatus.SOLD);

            seat.release();
            assertThat(seat.getSeatStatus()).isEqualTo(SeatStatus.AVAILABLE);
            assertThat(seat.isAvailable()).isTrue();

            seat.reserve();
            assertThat(seat.getSeatStatus()).isEqualTo(SeatStatus.RESERVED);
        }

        @Test
        @DisplayName("Should leave seat unchanged after a failed transition")
        void shouldLeaveSeatUnchangedAfterFailedTransition() {
            Seat seat = seatWithStatus(SeatStatus.SOLD);

            assertThatThrownBy(seat::reserve)
                    .isInstanceOf(InvalidSeatStateException.class);
            assertThatThrownBy(seat::sell)
                    .isInstanceOf(InvalidSeatStateException.class);

            assertThat(seat.getSeatStatus()).isEqualTo(SeatStatus.SOLD);
        }
    }

    @Nested
    @DisplayName("Availability Tests")
    class Availability {

        @Test
        @DisplayName("Should return true when status is AVAILABLE")
        void shouldReturnTrueWhenStatusIsAvailable() {
            Seat seat = seatWithStatus(SeatStatus.AVAILABLE);

            assertThat(seat.isAvailable()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = SeatStatus.class, names = {"RESERVED", "SOLD"})
        @DisplayName("Should return false when status is not AVAILABLE")
        void shouldReturnFalseWhenStatusIsNotAvailable(SeatStatus status) {
            Seat seat = seatWithStatus(status);

            assertThat(seat.isAvailable()).isFalse();
        }
    }

    private Seat seatWithStatus(SeatStatus status) {
        return Seat.restore(
                id,
                eventId,
                validSeatNumber,
                status
        );
    }
}
