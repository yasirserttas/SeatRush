package com.seatrush.ticket.application.service;

import com.seatrush.ticket.application.port.out.TicketQueryPort;
import com.seatrush.ticket.application.query.TicketView;
import com.seatrush.ticket.domain.exception.TicketNotFoundException;
import com.seatrush.ticket.domain.model.TicketStatus;
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
@DisplayName("TicketQueryService Unit Tests")
class TicketQueryServiceTest {

    @Mock
    private TicketQueryPort ticketQueryPort;

    @InjectMocks
    private TicketQueryService ticketQueryService;

    @Nested
    @DisplayName("FindById Tests")
    class FindById {

        @Test
        @DisplayName("should return ticket view when ticket is found by id")
        void shouldReturnTicketViewWhenTicketIsFoundById() {
            UUID ticketId = UUID.randomUUID();
            TicketView expectedView = new TicketView(
                    ticketId,
                    "TCK-ABC-123",
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    LocalDateTime.now(),
                    TicketStatus.VALID,
                    null,
                    null
            );

            when(ticketQueryPort.findById(ticketId)).thenReturn(Optional.of(expectedView));

            TicketView actualView = ticketQueryService.findById(ticketId);

            assertThat(actualView).isEqualTo(expectedView);
            verify(ticketQueryPort).findById(ticketId);
        }

        @Test
        @DisplayName("should throw TicketNotFoundException when ticket is not found by id")
        void shouldThrowTicketNotFoundExceptionWhenTicketIsNotFoundById() {
            UUID ticketId = UUID.randomUUID();

            when(ticketQueryPort.findById(ticketId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketQueryService.findById(ticketId))
                    .isInstanceOf(TicketNotFoundException.class);

            verify(ticketQueryPort).findById(ticketId);
        }
    }

    @Nested
    @DisplayName("FindByTicketCode Tests")
    class FindByTicketCode {

        @Test
        @DisplayName("should return ticket view when ticket is found by ticket code")
        void shouldReturnTicketViewWhenTicketIsFoundByTicketCode() {
            String ticketCode = "TCK-XYZ-999";
            TicketView expectedView = new TicketView(
                    UUID.randomUUID(),
                    ticketCode,
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    LocalDateTime.now(),
                    TicketStatus.VALID,
                    null,
                    null
            );

            when(ticketQueryPort.findByTicketCode(ticketCode)).thenReturn(Optional.of(expectedView));

            TicketView actualView = ticketQueryService.findByTicketCode(ticketCode);

            assertThat(actualView).isEqualTo(expectedView);
            verify(ticketQueryPort).findByTicketCode(ticketCode);
        }

        @Test
        @DisplayName("should throw TicketNotFoundException when ticket is not found by ticket code")
        void shouldThrowTicketNotFoundExceptionWhenTicketIsNotFoundByTicketCode() {
            String ticketCode = "NON-EXISTING-CODE";

            when(ticketQueryPort.findByTicketCode(ticketCode)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketQueryService.findByTicketCode(ticketCode))
                    .isInstanceOf(TicketNotFoundException.class);

            verify(ticketQueryPort).findByTicketCode(ticketCode);
        }
    }

    @Nested
    @DisplayName("FindByUserId Tests")
    class FindByUserId {

        @Test
        @DisplayName("should return ticket view list when tickets are found by user id")
        void shouldReturnTicketViewListWhenTicketsAreFoundByUserId() {
            UUID userId = UUID.randomUUID();
            List<TicketView> expectedViews = new ArrayList<>();

            for (int i = 1; i <= 3; i++) {
                expectedViews.add(new TicketView(
                        UUID.randomUUID(),
                        "TCK-CODE-" + i,
                        userId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        LocalDateTime.now(),
                        TicketStatus.VALID,
                        null,
                        null
                ));
            }

            when(ticketQueryPort.findByUserId(userId)).thenReturn(expectedViews);

            List<TicketView> actualViews = ticketQueryService.findByUserId(userId);

            assertThat(actualViews).containsExactlyElementsOf(expectedViews);
            verify(ticketQueryPort).findByUserId(userId);
        }

        @Test
        @DisplayName("should return empty list when no tickets found by user id")
        void shouldReturnEmptyListWhenNoTicketsFoundByUserId() {
            UUID userId = UUID.randomUUID();

            when(ticketQueryPort.findByUserId(userId)).thenReturn(List.of());

            List<TicketView> actualViews = ticketQueryService.findByUserId(userId);

            assertThat(actualViews).isEmpty();
            verify(ticketQueryPort).findByUserId(userId);
        }
    }
}
