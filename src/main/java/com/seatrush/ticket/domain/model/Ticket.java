package com.seatrush.ticket.domain.model;

import com.seatrush.ticket.domain.exception.InvalidTicketStateException;
import java.time.LocalDateTime;
import java.util.UUID;



public class Ticket {

    private final UUID id;
    private final String ticketCode;
    private final UUID userId;
    private final UUID seatId;
    private final UUID eventId;
    private final LocalDateTime issuedAt;

    private TicketStatus ticketStatus;
    private LocalDateTime usedAt;
    private LocalDateTime cancelledAt;

    private Ticket(
            UUID id,
            String ticketCode,
            UUID userId,
            UUID eventId,
            UUID seatId,
            TicketStatus ticketStatus,
            LocalDateTime issuedAt,
            LocalDateTime usedAt,
            LocalDateTime cancelledAt
    ) {
        validateId(id);
        validateTicketCode(ticketCode);
        validateReferences(userId, eventId, seatId);
        validateStatus(ticketStatus);
        validateStateAndDates(ticketStatus, issuedAt, usedAt, cancelledAt);

        this.id = id;
        this.ticketCode = ticketCode;
        this.userId = userId;
        this.eventId = eventId;
        this.seatId = seatId;
        this.ticketStatus = ticketStatus;
        this.issuedAt = issuedAt;
        this.usedAt = usedAt;
        this.cancelledAt = cancelledAt;
    }

    public static Ticket create(
            UUID userId,
            UUID eventId,
            UUID seatId
    ) {
        return new Ticket(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                userId,
                eventId,
                seatId,
                TicketStatus.VALID,
                LocalDateTime.now(),
                null,
                null
        );
    }

    public static Ticket restore(
            UUID id,
            String ticketCode,
            UUID userId,
            UUID eventId,
            UUID seatId,
            TicketStatus ticketStatus,
            LocalDateTime issuedAt,
            LocalDateTime usedAt,
            LocalDateTime cancelledAt
    ) {
        return new Ticket(
                id,
                ticketCode,
                userId,
                eventId,
                seatId,
                ticketStatus,
                issuedAt,
                usedAt,
                cancelledAt
        );
    }

    public void use() {
        validateCanBeUsed();

        this.ticketStatus = TicketStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    public void cancel() {
        validateCanBeCancelled();

        this.ticketStatus = TicketStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public boolean isValid() {
        return ticketStatus == TicketStatus.VALID;
    }

    public boolean isUsed() {
        return ticketStatus == TicketStatus.USED;
    }

    public boolean isCancelled() {
        return ticketStatus == TicketStatus.CANCELLED;
    }

    public boolean holdsSeat() {
        return ticketStatus == TicketStatus.VALID;
    }

    private void validateCanBeUsed() {
        if (this.ticketStatus == TicketStatus.USED) {
            throw new InvalidTicketStateException("Ticket is already used");
        }
        if (this.ticketStatus == TicketStatus.CANCELLED) {
            throw new InvalidTicketStateException("Cancelled ticket cannot be used");
        }
    }

    private void validateCanBeCancelled() {
        if (this.ticketStatus == TicketStatus.CANCELLED) {
            throw new InvalidTicketStateException("Ticket is already cancelled");
        }
        if (this.ticketStatus == TicketStatus.USED) {
            throw new InvalidTicketStateException("Used ticket cannot be cancelled");
        }
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
    }

    private static void validateTicketCode(String ticketCode) {
        if (ticketCode == null || ticketCode.isBlank()) {
            throw new IllegalArgumentException("ticketCode cannot be blank");
        }
    }

    private static void validateReferences(UUID userId, UUID eventId, UUID seatId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (eventId == null) {
            throw new IllegalArgumentException("eventId cannot be null");
        }
        if (seatId == null) {
            throw new IllegalArgumentException("seatId cannot be null");
        }
    }

    private static void validateStatus(TicketStatus ticketStatus) {
        if (ticketStatus == null) {
            throw new IllegalArgumentException("ticketStatus cannot be null");
        }
    }

    private static void validateStateAndDates(
            TicketStatus status,
            LocalDateTime issuedAt,
            LocalDateTime usedAt,
            LocalDateTime cancelledAt
    ) {
        validateDates(issuedAt, usedAt, cancelledAt);

        if (status == null) {
            return;
        }

        switch (status) {
            case VALID -> {
                if (usedAt != null) {
                    throw new IllegalArgumentException("VALID ticket cannot have usedAt date");
                }
                if (cancelledAt != null) {
                    throw new IllegalArgumentException("VALID ticket cannot have cancelledAt date");
                }
            }
            case USED -> {
                if (usedAt == null) {
                    throw new IllegalArgumentException("USED ticket must have usedAt date");
                }
                if (cancelledAt != null) {
                    throw new IllegalArgumentException(
                            "USED ticket cannot have cancelledAt date"
                    );
                }
            }
            case CANCELLED -> {
                if (cancelledAt == null) {
                    throw new IllegalArgumentException("CANCELLED ticket must have cancelledAt date");
                }
                if (usedAt != null) {
                    throw new IllegalArgumentException("CANCELLED ticket cannot have usedAt date");
                }
            }
        }
    }

    private static void validateDates(
            LocalDateTime issuedAt,
            LocalDateTime usedAt,
            LocalDateTime cancelledAt
    ) {
        if (issuedAt == null) {
            throw new IllegalArgumentException(
                    "issuedAt cannot be null"
            );
        }

        if (usedAt != null && !usedAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException(
                    "usedAt must be after issuedAt"
            );
        }

        if (cancelledAt != null && !cancelledAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException(
                    "cancelledAt must be after issuedAt"
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getSeatId() {
        return seatId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public TicketStatus getTicketStatus() {
        return ticketStatus;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
}