package com.seatrush.seat.domain.model;

import com.seatrush.seat.domain.exception.InvalidSeatStateException;

import java.util.UUID;

public class Seat {

    private final UUID id;
    private final UUID eventId;
    private final int seatNumber;
    private SeatStatus seatStatus;

    private Seat(
            UUID id,
            UUID eventId,
            int seatNumber,
            SeatStatus seatStatus
    ) {
        validate(eventId, seatNumber);

        if (id == null) {
            throw new IllegalArgumentException("Seat id cannot be null");
        }

        if (seatStatus == null) {
            throw new IllegalArgumentException("Seat status cannot be null");
        }

        this.id = id;
        this.eventId = eventId;
        this.seatNumber = seatNumber;
        this.seatStatus = seatStatus;
    }

    public static Seat create(UUID eventId, int seatNumber) {
        validate(eventId, seatNumber);
        return new Seat(
                UUID.randomUUID(),
                eventId,
                seatNumber,
                SeatStatus.AVAILABLE
        );
    }

    public static Seat restore(
            UUID id,
            UUID eventId,
            int seatNumber,
            SeatStatus seatStatus
    ) {
        validate(eventId, seatNumber);
        return new Seat(id, eventId, seatNumber, seatStatus);
    }

    public void reserve() {
        if (seatStatus != SeatStatus.AVAILABLE) {
            throw new InvalidSeatStateException(
                    "only available seats can be reserved"
            );
        }

        this.seatStatus = SeatStatus.RESERVED;
    }

    public void sell() {
        if (seatStatus != SeatStatus.RESERVED && seatStatus != SeatStatus.AVAILABLE) {
            throw new InvalidSeatStateException(
                    "Only available or reserved seats can be sold"
            );
        }

        this.seatStatus = SeatStatus.SOLD;
    }

    public void release() {
        if (seatStatus != SeatStatus.RESERVED && seatStatus != SeatStatus.SOLD) {
            throw new InvalidSeatStateException(
                    "Only reserved or sold seats can be released"
            );
        }

        this.seatStatus = SeatStatus.AVAILABLE;
    }

    public boolean isAvailable() {
        return seatStatus == SeatStatus.AVAILABLE;
    }

    private static void validate(UUID eventId, int seatNumber) {
        if (eventId == null) {
            throw new IllegalArgumentException(
                    "Event id cannot be null"
            );
        }

        if ( seatNumber <= 0 ) {
            throw new IllegalArgumentException(
                    "Seat number must be greater than zero"
            );
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public SeatStatus getSeatStatus() {
        return seatStatus;
    }
}