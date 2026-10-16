package com.seatrush.ticket.application.service;

import com.seatrush.ticket.application.command.CreateTicketCommand;
import com.seatrush.ticket.application.port.out.SeatReservationPort;
import com.seatrush.ticket.application.port.out.TicketRepository;
import com.seatrush.ticket.domain.exception.InvalidTicketStateException;
import com.seatrush.ticket.domain.exception.TicketNotFoundException;
import com.seatrush.ticket.domain.model.Ticket;
import com.seatrush.ticket.domain.model.TicketStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketCommandService Unit Tests")
class TicketCommandServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private SeatReservationPort seatReservationPort;

    @InjectMocks
    private TicketCommandService ticketCommandService;

    @Nested
    @DisplayName("Create Tests")
    class Create {

        @Test
        @DisplayName("should create, save ticket, reserve seat and return ticket id when command is valid")
        void shouldCreateSaveTicketReserveSeatAndReturnTicketIdWhenCommandIsValid() {
            UUID userId = UUID.randomUUID();
            UUID seatId = UUID.randomUUID();
            UUID eventId = UUID.randomUUID();

            CreateTicketCommand command = new CreateTicketCommand(userId, seatId, eventId);
            ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);

            when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UUID actualTicketId = ticketCommandService.create(command);

            verify(seatReservationPort).sell(seatId);
            verify(ticketRepository).save(ticketCaptor.capture());

            Ticket savedTicket = ticketCaptor.getValue();

            assertThat(savedTicket.getUserId()).isEqualTo(userId);
            assertThat(savedTicket.getEventId()).isEqualTo(eventId);
            assertThat(savedTicket.getSeatId()).isEqualTo(seatId);
            assertThat(savedTicket.getTicketStatus()).isEqualTo(TicketStatus.VALID);
            assertThat(actualTicketId).isEqualTo(savedTicket.getId());
        }

        @Test
        @DisplayName("should return id of the saved ticket instance, not the built one")
        void shouldReturnIdOfSavedTicketInstance() {
            UUID seatId = UUID.randomUUID();
            CreateTicketCommand command =
                    new CreateTicketCommand(UUID.randomUUID(), seatId, UUID.randomUUID());

            Ticket persistedTicket = Ticket.create(
                    UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
            when(ticketRepository.save(any(Ticket.class))).thenReturn(persistedTicket);

            UUID actualTicketId = ticketCommandService.create(command);

            assertThat(actualTicketId).isEqualTo(persistedTicket.getId());
        }

        @Test
        @DisplayName("should not save ticket and propagate when seat sell fails")
        void shouldNotSaveTicketAndPropagateWhenSeatSellFails() {
            UUID seatId = UUID.randomUUID();
            CreateTicketCommand command =
                    new CreateTicketCommand(UUID.randomUUID(), seatId, UUID.randomUUID());

            doThrow(new RuntimeException("seat not available"))
                    .when(seatReservationPort).sell(seatId);

            assertThatThrownBy(() -> ticketCommandService.create(command))
                    .isInstanceOf(RuntimeException.class);

            verify(ticketRepository, never()).save(any(Ticket.class));
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class Delete {

        @Test
        @DisplayName("should delete ticket when ticket is found by id")
        void shouldDeleteTicketWhenTicketIsFoundById() {
            UUID ticketId = UUID.randomUUID();
            Ticket ticket = Ticket.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

            ticketCommandService.delete(ticketId);

            verify(ticketRepository).findById(ticketId);
            verify(seatReservationPort).release(ticket.getSeatId());
            verify(ticketRepository).delete(ticket.getId());
        }

        @Test
        @DisplayName("should throw TicketNotFoundException when deleting ticket that is not found by id")
        void shouldThrowTicketNotFoundExceptionWhenDeletingTicketThatIsNotFoundById() {
            UUID ticketId = UUID.randomUUID();

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketCommandService.delete(ticketId))
                    .isInstanceOf(TicketNotFoundException.class);

            verify(ticketRepository).findById(ticketId);
            verify(ticketRepository, never()).delete(any(UUID.class));
            verify(seatReservationPort, never()).release(any());
        }

        @Test
        @DisplayName("should release the seat before deleting the ticket")
        void shouldReleaseSeatBeforeDeletingTicket() {
            Ticket ticket = Ticket.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

            when(ticketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

            ticketCommandService.delete(ticket.getId());

            InOrder inOrder = inOrder(seatReservationPort, ticketRepository);
            inOrder.verify(seatReservationPort).release(ticket.getSeatId());
            inOrder.verify(ticketRepository).delete(ticket.getId());
        }
    }

    @Nested
    @DisplayName("Delete Defect Tests (documented production defects)")
    class DeleteDefects {

        @Test
        @DisplayName("DEFECT: deleting a USED ticket must not free its seat (seat can be resold)")
        void shouldNotReleaseSeatWhenDeletingUsedTicket() {
            LocalDateTime issuedAt = LocalDateTime.now().minusDays(2);
            Ticket usedTicket = Ticket.restore(
                    UUID.randomUUID(),
                    "TCK-USED",
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    TicketStatus.USED,
                    issuedAt,
                    LocalDateTime.now().minusDays(1),
                    null
            );

            when(ticketRepository.findById(usedTicket.getId()))
                    .thenReturn(Optional.of(usedTicket));

            ticketCommandService.delete(usedTicket.getId());

            verify(seatReservationPort, never()).release(any());
            verify(ticketRepository).delete(usedTicket.getId());
        }

        @Test
        @DisplayName("DEFECT: deleting a CANCELLED ticket must not release the seat again (seat is already free -> release throws downstream)")
        void shouldNotReleaseSeatWhenDeletingCancelledTicket() {
            LocalDateTime issuedAt = LocalDateTime.now().minusDays(2);
            Ticket cancelledTicket = Ticket.restore(
                    UUID.randomUUID(),
                    "TCK-CXL",
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    TicketStatus.CANCELLED,
                    issuedAt,
                    null,
                    LocalDateTime.now().minusDays(1)
            );

            when(ticketRepository.findById(cancelledTicket.getId()))
                    .thenReturn(Optional.of(cancelledTicket));

            ticketCommandService.delete(cancelledTicket.getId());

            verify(seatReservationPort, never()).release(any());
            verify(ticketRepository).delete(cancelledTicket.getId());
        }
    }

    @Nested
    @DisplayName("UseTicket Tests")
    class UseTicket {

        @Test
        @DisplayName("should use ticket and save when ticket is found by id")
        void shouldUseTicketAndSaveWhenTicketIsFoundById() {
            UUID ticketId = UUID.randomUUID();
            Ticket ticket = Ticket.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

            ticketCommandService.useTicket(ticketId);

            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.USED);
            assertThat(ticket.getUsedAt()).isNotNull();

            verify(ticketRepository).findById(ticketId);
            verify(ticketRepository).save(ticket);
            verifyNoInteractions(seatReservationPort);
        }

        @Test
        @DisplayName("should throw TicketNotFoundException when using ticket that is not found by id")
        void shouldThrowTicketNotFoundExceptionWhenUsingTicketThatIsNotFoundById() {
            UUID ticketId = UUID.randomUUID();

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketCommandService.useTicket(ticketId))
                    .isInstanceOf(TicketNotFoundException.class);

            verify(ticketRepository).findById(ticketId);
            verify(ticketRepository, never()).save(any(Ticket.class));
        }

        @ParameterizedTest
        @EnumSource(value = TicketStatus.class,
                names = {"VALID"},
                mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("should throw InvalidTicketStateException when using ticket that status is not valid")
        void shouldThrowInvalidTicketStateExceptionWhenUsingTicketThatStatusIsNotValid(TicketStatus status) {
            UUID ticketId = UUID.randomUUID();
            LocalDateTime issuedAt = LocalDateTime.now().minusDays(2);
            LocalDateTime date = LocalDateTime.now().minusDays(1);

            Ticket ticket = Ticket.restore(
                    ticketId,
                    "TCK-123",
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    status,
                    issuedAt,
                    status == TicketStatus.USED ? date : null,
                    status == TicketStatus.CANCELLED ? date : null
            );

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> ticketCommandService.useTicket(ticketId))
                    .isInstanceOf(InvalidTicketStateException.class);

            verify(ticketRepository).findById(ticketId);
            verify(ticketRepository, never()).save(any(Ticket.class));
        }
    }

    @Nested
    @DisplayName("CancelTicket Tests")
    class CancelTicket {

        @Test
        @DisplayName("should cancel ticket, release seat and save when ticket is found by id")
        void shouldCancelTicketReleaseSeatAndSaveWhenTicketIsFoundById() {
            UUID ticketId = UUID.randomUUID();
            Ticket ticket = Ticket.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

            ticketCommandService.cancelTicket(ticketId);

            assertThat(ticket.getTicketStatus()).isEqualTo(TicketStatus.CANCELLED);
            assertThat(ticket.getCancelledAt()).isNotNull();

            verify(seatReservationPort).release(ticket.getSeatId());
            verify(ticketRepository).findById(ticketId);
            verify(ticketRepository).save(ticket);
        }

        @Test
        @DisplayName("should not save ticket and propagate when seat release fails during cancel")
        void shouldNotSaveTicketAndPropagateWhenSeatReleaseFailsDuringCancel() {
            UUID ticketId = UUID.randomUUID();
            Ticket ticket = Ticket.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
            doThrow(new RuntimeException("seat release failed"))
                    .when(seatReservationPort).release(ticket.getSeatId());

            assertThatThrownBy(() -> ticketCommandService.cancelTicket(ticketId))
                    .isInstanceOf(RuntimeException.class);

            verify(ticketRepository, never()).save(any(Ticket.class));
        }

        @Test
        @DisplayName("should throw TicketNotFoundException when cancelling ticket that is not found by id")
        void shouldThrowTicketNotFoundExceptionWhenCancellingTicketThatIsNotFoundById() {
            UUID ticketId = UUID.randomUUID();

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketCommandService.cancelTicket(ticketId))
                    .isInstanceOf(TicketNotFoundException.class);

            verify(ticketRepository).findById(ticketId);
            verify(seatReservationPort, never()).release(any());
            verify(ticketRepository, never()).save(any(Ticket.class));
        }

        @ParameterizedTest
        @EnumSource(value = TicketStatus.class,
                names = {"VALID"},
                mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("should throw InvalidTicketStateException when cancelling ticket that status is not valid")
        void shouldThrowInvalidTicketStateExceptionWhenCancellingTicketThatStatusIsNotValid(TicketStatus status) {
            UUID ticketId = UUID.randomUUID();
            LocalDateTime issuedAt = LocalDateTime.now().minusDays(2);
            LocalDateTime date = LocalDateTime.now().minusDays(1);

            Ticket ticket = Ticket.restore(
                    ticketId,
                    "TCK-123",
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    status,
                    issuedAt,
                    status == TicketStatus.USED ? date : null,
                    status == TicketStatus.CANCELLED ? date : null
            );

            when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> ticketCommandService.cancelTicket(ticketId))
                    .isInstanceOf(InvalidTicketStateException.class);

            verify(ticketRepository).findById(ticketId);
            verify(seatReservationPort, never()).release(any());
            verify(ticketRepository, never()).save(any(Ticket.class));
        }
    }
}
