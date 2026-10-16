package com.seatrush.ticket.infrastructure.persistence;

import com.seatrush.ticket.domain.model.TicketStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "tickets",
uniqueConstraints = {
        @UniqueConstraint(name = "uk_ticket_code",
        columnNames = "ticket_code")
})
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "ticket_code", nullable = false, updatable = false)
    private String ticketCode;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "seat_id", nullable = false, updatable = false)
    private UUID seatId;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status", nullable = false)
    private TicketStatus ticketStatus;

    @CreationTimestamp
    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;


}