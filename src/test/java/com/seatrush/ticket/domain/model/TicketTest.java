package com.seatrush.ticket.domain.model;

import com.seatrush.ticket.domain.exception.InvalidTicketStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Ticket Domain Model Unit Tests")
public class TicketTest {

    private final UUID id = UUID.randomUUID();
    private final UUID eventId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID seatId = UUID.randomUUID();
    private final String ticketCode = UUID.randomUUID().toString();
    private final LocalDateTime issuedAt = LocalDateTime.of(2026, 8, 20, 15, 0);
    private final LocalDateTime usedAt = LocalDateTime.of(2026, 8, 20, 16, 0);
    private final LocalDateTime cancelledAt = LocalDateTime.of(2026, 8, 20, 16, 0);

    @Nested
    @DisplayName("Creation Tests")
    class Creation {

        @Test
        @DisplayName("Should create ticket with VALID status when valid parameters provided")
        void shouldCreateTicketWithValidStatusWhenValidParametersProvided() {
            Ticket ticket = Ticket.create(userId, eventId, seatId);

            assertThat(ticket.getId()).isNotNull();
            assertThat(ticket.getEventId()).isEqualTo(eventId);
            assertThat(ticket.getSeatId()).isEqualTo(seatId);
            assertThat(ticket.getUserId()).isEqualTo(userId);
            assertThat(ticket.getTicketCode()).isNotNull();
            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.VALID);
            assertThat(ticket.getIssuedAt()).isNotNull();
            assertThat(ticket.getCancelledAt()).isNull();
            assertThat(ticket.getUsedAt()).isNull();
            assertThat(ticket.isValid()).isTrue();
            assertThat(ticket.isUsed()).isFalse();
            assertThat(ticket.isCancelled()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when userId is null")
        void shouldThrowExceptionWhenUserIdIsNull() {
            assertThatThrownBy(() -> Ticket.create(null, eventId, seatId))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when eventId is null")
        void shouldThrowExceptionWhenEventIdIsNull() {
            assertThatThrownBy(() -> Ticket.create(userId, null, seatId))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when seatId is null")
        void shouldThrowExceptionWhenSeatIdIsNull() {
            assertThatThrownBy(() -> Ticket.create(userId, eventId, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should generate unique ids and ticket codes across creates")
        void shouldGenerateUniqueIdsAndTicketCodesAcrossCreates() {
            Ticket first = Ticket.create(userId, eventId, seatId);
            Ticket second = Ticket.create(userId, eventId, seatId);

            assertThat(first.getId()).isNotEqualTo(second.getId());
            assertThat(first.getTicketCode()).isNotEqualTo(second.getTicketCode());
            assertThat(first.getTicketCode()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("Restore Tests")
    class Restore {

        @Test
        @DisplayName("Should restore VALID ticket when valid state and dates provided")
        void shouldRestoreValidTicketWhenValidStateProvided() {
            Ticket ticket = Ticket.restore(
                    id,
                    ticketCode,
                    userId,
                    eventId,
                    seatId,
                    TicketStatus.VALID,
                    issuedAt,
                    null,
                    null
            );

            assertThat(ticket.getId()).isEqualTo(id);
            assertThat(ticket.getTicketCode()).isEqualTo(ticketCode);
            assertThat(ticket.getUserId()).isEqualTo(userId);
            assertThat(ticket.getEventId()).isEqualTo(eventId);
            assertThat(ticket.getSeatId()).isEqualTo(seatId);
            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.VALID);
            assertThat(ticket.getIssuedAt()).isEqualTo(issuedAt);
            assertThat(ticket.getUsedAt()).isNull();
            assertThat(ticket.getCancelledAt()).isNull();
            assertThat(ticket.isValid()).isTrue();
            assertThat(ticket.isUsed()).isFalse();
            assertThat(ticket.isCancelled()).isFalse();
        }

        @Test
        @DisplayName("Should restore USED ticket when valid state and usedAt provided")
        void shouldRestoreUsedTicketWhenValidStateAndUsedAtProvided() {
            Ticket ticket = Ticket.restore(
                    id,
                    ticketCode,
                    userId,
                    eventId,
                    seatId,
                    TicketStatus.USED,
                    issuedAt,
                    usedAt,
                    null
            );

            assertThat(ticket.getId()).isEqualTo(id);
            assertThat(ticket.getTicketCode()).isEqualTo(ticketCode);
            assertThat(ticket.getUserId()).isEqualTo(userId);
            assertThat(ticket.getEventId()).isEqualTo(eventId);
            assertThat(ticket.getSeatId()).isEqualTo(seatId);
            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.USED);
            assertThat(ticket.getIssuedAt()).isEqualTo(issuedAt);
            assertThat(ticket.getUsedAt()).isEqualTo(usedAt);
            assertThat(ticket.getCancelledAt()).isNull();
            assertThat(ticket.isValid()).isFalse();
            assertThat(ticket.isUsed()).isTrue();
            assertThat(ticket.isCancelled()).isFalse();
        }

        @Test
        @DisplayName("Should restore CANCELLED ticket when valid state and cancelledAt provided")
        void shouldRestoreCancelledTicketWhenValidStateAndCancelledAtProvided() {
            Ticket ticket = Ticket.restore(
                    id,
                    ticketCode,
                    userId,
                    eventId,
                    seatId,
                    TicketStatus.CANCELLED,
                    issuedAt,
                    null,
                    cancelledAt
            );

            assertThat(ticket.getId()).isEqualTo(id);
            assertThat(ticket.getTicketCode()).isEqualTo(ticketCode);
            assertThat(ticket.getUserId()).isEqualTo(userId);
            assertThat(ticket.getEventId()).isEqualTo(eventId);
            assertThat(ticket.getSeatId()).isEqualTo(seatId);
            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.CANCELLED);
            assertThat(ticket.getIssuedAt()).isEqualTo(issuedAt);
            assertThat(ticket.getUsedAt()).isNull();
            assertThat(ticket.getCancelledAt()).isEqualTo(cancelledAt);
            assertThat(ticket.isValid()).isFalse();
            assertThat(ticket.isUsed()).isFalse();
            assertThat(ticket.isCancelled()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when restored with null id")
        void shouldThrowExceptionWhenRestoredWithNullId() {
            assertThatThrownBy(() -> Ticket.restore(
                    null,
                    ticketCode,
                    userId,
                    eventId,
                    seatId,
                    TicketStatus.VALID,
                    issuedAt,
                    null,
                    null
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Should throw exception when restored with null or blank ticketCode")
        void shouldThrowExceptionWhenRestoredWithNullOrBlankTicketCode(String invalidTicketCode) {
            assertThatThrownBy(() -> Ticket.restore(
                    id,
                    invalidTicketCode,
                    userId,
                    eventId,
                    seatId,
                    TicketStatus.VALID,
                    issuedAt,
                    null,
                    null
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored with null userId")
        void shouldThrowExceptionWhenRestoredWithNullUserId() {
            assertThatThrownBy(() -> Ticket.restore(
                    id,
                    ticketCode,
                    null,
                    eventId,
                    seatId,
                    TicketStatus.VALID,
                    issuedAt,
                    null,
                    null
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored with null eventId")
        void shouldThrowExceptionWhenRestoredWithNullEventId() {
            assertThatThrownBy(() -> Ticket.restore(
                    id,
                    ticketCode,
                    userId,
                    null,
                    seatId,
                    TicketStatus.VALID,
                    issuedAt,
                    null,
                    null
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored with null seatId")
        void shouldThrowExceptionWhenRestoredWithNullSeatId() {
            assertThatThrownBy(() -> Ticket.restore(
                    id,
                    ticketCode,
                    userId,
                    eventId,
                    null,
                    TicketStatus.VALID,
                    issuedAt,
                    null,
                    null
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored with null status")
        void shouldThrowExceptionWhenRestoredWithNullStatus() {
            assertThatThrownBy(() -> Ticket.restore(
                    id,
                    ticketCode,
                    userId,
                    eventId,
                    seatId,
                    null,
                    issuedAt,
                    null,
                    null
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored with null issuedAt")
        void shouldThrowExceptionWhenRestoredWithNullIssuedAt() {
            assertThatThrownBy(() -> Ticket.restore(
                    id,
                    ticketCode,
                    userId,
                    eventId,
                    seatId,
                    TicketStatus.VALID,
                    null,
                    null,
                    null
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored usedAt is equal to issuedAt")
        void shouldThrowExceptionWhenRestoredUsedAtEqualsIssuedAt() {
            LocalDateTime sameTime = issuedAt;

            assertThatThrownBy(() -> Ticket.restore(
                    id,
                    ticketCode,
                    userId,
                    eventId,
                    seatId,
                    TicketStatus.USED,
                    sameTime,
                    sameTime,
                    null
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored usedAt is before issuedAt")
        void shouldThrowExceptionWhenRestoredUsedAtBeforeIssuedAt() {
            LocalDateTime pastTime = issuedAt.minusHours(1);

            assertThatThrownBy(() -> Ticket.restore(
                    id,
                    ticketCode,
                    userId,
                    eventId,
                    seatId,
                    TicketStatus.USED,
                    issuedAt,
                    pastTime,
                    null
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored cancelledAt is equal to issuedAt")
        void shouldThrowExceptionWhenRestoredCancelledAtEqualsIssuedAt() {
            LocalDateTime sameTime = issuedAt;

            assertThatThrownBy(() -> Ticket.restore(
                    id,
                    ticketCode,
                    userId,
                    eventId,
                    seatId,
                    TicketStatus.CANCELLED,
                    sameTime,
                    null,
                    sameTime
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored cancelledAt is before issuedAt")
        void shouldThrowExceptionWhenRestoredCancelledAtBeforeIssuedAt() {
            LocalDateTime pastTime = issuedAt.minusHours(1);

            assertThatThrownBy(() -> Ticket.restore(
                    id,
                    ticketCode,
                    userId,
                    eventId,
                    seatId,
                    TicketStatus.CANCELLED,
                    issuedAt,
                    null,
                    pastTime
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored VALID status with non-null usedAt")
        void shouldThrowExceptionWhenRestoredValidStatusWithNonNullUsedAt() {
            assertThatThrownBy(() -> Ticket.restore(
                    id,
                    ticketCode,
                    userId,
                    eventId,
                    seatId,
                    TicketStatus.VALID,
                    issuedAt,
                    usedAt,
                    null
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored VALID status with non-null cancelledAt")
        void shouldThrowExceptionWhenRestoredValidStatusWithNonNullCancelledAt() {
            assertThatThrownBy(() -> Ticket.restore(
                    id,
                    ticketCode,
                    userId,
                    eventId,
                    seatId,
                    TicketStatus.VALID,
                    issuedAt,
                    null,
                    cancelledAt
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored VALID status with both usedAt and cancelledAt")
        void shouldThrowExceptionWhenRestoredValidStatusWithBothDates() {
            assertThatThrownBy(() -> Ticket.restore(
                    id,
                    ticketCode,
                    userId,
                    eventId,
                    seatId,
                    TicketStatus.VALID,
                    issuedAt,
                    usedAt,
                    cancelledAt
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored USED status with null usedAt")
        void shouldThrowExceptionWhenRestoredUsedStatusWithNullUsedAt() {
            assertThatThrownBy(() -> Ticket.restore(
                    id,
                    ticketCode,
                    userId,
                    eventId,
                    seatId,
                    TicketStatus.USED,
                    issuedAt,
                    null,
                    null
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored USED status with non-null cancelledAt")
        void shouldThrowExceptionWhenRestoredUsedStatusWithNonNullCancelledAt() {
            assertThatThrownBy(() -> Ticket.restore(
                    id,
                    ticketCode,
                    userId,
                    eventId,
                    seatId,
                    TicketStatus.USED,
                    issuedAt,
                    usedAt,
                    cancelledAt
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored CANCELLED status with null cancelledAt")
        void shouldThrowExceptionWhenRestoredCancelledStatusWithNullCancelledAt() {
            assertThatThrownBy(() -> Ticket.restore(
                    id,
                    ticketCode,
                    userId,
                    eventId,
                    seatId,
                    TicketStatus.CANCELLED,
                    issuedAt,
                    null,
                    null
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when restored CANCELLED status with non-null usedAt")
        void shouldThrowExceptionWhenRestoredCancelledStatusWithNonNullUsedAt() {
            assertThatThrownBy(() -> Ticket.restore(
                    id,
                    ticketCode,
                    userId,
                    eventId,
                    seatId,
                    TicketStatus.CANCELLED,
                    issuedAt,
                    usedAt,
                    cancelledAt
            )).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Use Tests")
    class Use {

        @Test
        @DisplayName("Should use ticket when status is valid")
        void shouldUseTicketWhenStatusIsValid() {
            Ticket ticket = withStatus(TicketStatus.VALID);

            ticket.use();

            assertThat(ticket.getUsedAt()).isNotNull();
            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.USED);
        }

        @Test
        @DisplayName("Should reject when used ticket is used again")
        void shouldRejectWhenUsedTicketIsUsedAgain() {
            Ticket ticket = withStatus(TicketStatus.VALID);
            ticket.use();

            LocalDateTime firstUseAt = ticket.getUsedAt();

            assertThatThrownBy(ticket::use).isInstanceOf(InvalidTicketStateException.class);
            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.USED);
            assertThat(ticket.getUsedAt()).isEqualTo(firstUseAt);
        }

        @Test
        @DisplayName("Should reject when cancelled ticket is used")
        void shouldRejectWhenCancelledTicketIsUsed() {
            Ticket ticket = withStatus(TicketStatus.VALID);
            ticket.cancel();

            assertThatThrownBy(ticket::use).isInstanceOf(InvalidTicketStateException.class);
            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.CANCELLED);
            assertThat(ticket.getUsedAt()).isNull();
            assertThat(ticket.getCancelledAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Cancel Tests")
    class Cancel {

        @Test
        @DisplayName("Should cancel ticket when status is valid")
        void shouldCancelTicketWhenStatusIsValid() {
            Ticket ticket = withStatus(TicketStatus.VALID);

            ticket.cancel();

            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.CANCELLED);
            assertThat(ticket.getUsedAt()).isNull();
            assertThat(ticket.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("Should reject when cancelled ticket is cancelled again")
        void shouldRejectWhenCancelledTicketIsCancelledAgain() {
            Ticket ticket = withStatus(TicketStatus.VALID);
            ticket.cancel();

            LocalDateTime firstCancelAt = ticket.getCancelledAt();

            assertThatThrownBy(ticket::cancel).isInstanceOf(InvalidTicketStateException.class);
            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.CANCELLED);
            assertThat(ticket.getCancelledAt()).isEqualTo(firstCancelAt);
        }

        @Test
        @DisplayName("Should reject when used ticket is cancelled")
        void shouldRejectWhenUsedTicketIsCancelled() {
            Ticket ticket = withStatus(TicketStatus.VALID);
            ticket.use();

            LocalDateTime firstUsedAt = ticket.getUsedAt();

            assertThatThrownBy(ticket::cancel).isInstanceOf(InvalidTicketStateException.class);
            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.USED);
            assertThat(ticket.getUsedAt()).isEqualTo(firstUsedAt);
            assertThat(ticket.getCancelledAt()).isNull();
        }
    }

    @Nested
    @DisplayName("StatusChecks Tests")
    class StatusChecks {

        @Test
        @DisplayName("Should return true when ticket status is valid")
        void shouldReturnTrueWhenTicketStatusIsValid() {
            Ticket ticket = withStatus(TicketStatus.VALID);

            assertThat(ticket.isValid()).isTrue();
            assertThat(ticket.isUsed()).isFalse();
            assertThat(ticket.isCancelled()).isFalse();
        }

        @Test
        @DisplayName("Should return true when ticket status is used")
        void shouldReturnTrueWhenTicketStatusIsUsed() {
            Ticket ticket = withStatus(TicketStatus.VALID);
            ticket.use();

            assertThat(ticket.isValid()).isFalse();
            assertThat(ticket.isUsed()).isTrue();
            assertThat(ticket.isCancelled()).isFalse();
        }

        @Test
        @DisplayName("Should return true when ticket status is cancelled")
        void shouldReturnTrueWhenTicketStatusIsCancelled() {
            Ticket ticket = withStatus(TicketStatus.VALID);
            ticket.cancel();

            assertThat(ticket.isValid()).isFalse();
            assertThat(ticket.isUsed()).isFalse();
            assertThat(ticket.isCancelled()).isTrue();
        }

        @Test
        @DisplayName("Should hold the seat only while the ticket is VALID")
        void shouldHoldSeatOnlyWhileTicketIsValid() {
            Ticket ticket = withStatus(TicketStatus.VALID);
            assertThat(ticket.holdsSeat()).isTrue();

            ticket.cancel();
            assertThat(ticket.holdsSeat()).isFalse();
        }

        @Test
        @DisplayName("Should not hold the seat once the ticket is used")
        void shouldNotHoldSeatOnceTicketIsUsed() {
            Ticket ticket = withStatus(TicketStatus.VALID);
            ticket.use();

            assertThat(ticket.holdsSeat()).isFalse();
        }
    }

    private Ticket withStatus(TicketStatus status) {
        return Ticket.restore(id, ticketCode, userId, eventId, seatId, status, issuedAt, null, null);
    }
}
