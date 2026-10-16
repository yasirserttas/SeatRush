package com.seatrush.seat.infrastructure.persistence;

import com.seatrush.seat.domain.model.SeatStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(
        name = "seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_event_seat_number",
                        columnNames = {"event_id", "seat_number"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_seat_event_id",
                        columnList = "event_id"
                ),
                @Index(
                        name = "idx_seat_status",
                        columnList = "seat_status"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatJpaEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(
            name = "event_id",
            nullable = false,
            updatable = false
    )
    private UUID eventId;

    @Column(
            name = "seat_number",
            nullable = false,
            updatable = false
    )
    private int seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "seat_status",
            nullable = false
    )
    private SeatStatus seatStatus;

    public SeatJpaEntity(
            UUID id,
            UUID eventId,
            int seatNumber,
            SeatStatus seatStatus
    ) {
        this.id = id;
        this.eventId = eventId;
        this.seatNumber = seatNumber;
        this.seatStatus = seatStatus;
    }
}