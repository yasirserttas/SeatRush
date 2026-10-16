package com.seatrush.seat.application.service;

import com.seatrush.seat.application.command.CreateSeatCommand;
import com.seatrush.seat.application.command.CreateSeatsForEventCommand;
import com.seatrush.seat.application.port.in.SeatCommandUseCase;
import com.seatrush.seat.application.port.out.SeatRepository;
import com.seatrush.seat.domain.exception.SeatNotFoundException;
import com.seatrush.seat.domain.model.Seat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class SeatCommandService implements SeatCommandUseCase {

    private final SeatRepository seatRepository;

    public SeatCommandService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }


    @Override
    public UUID create(CreateSeatCommand command) {

        Seat seat = Seat.create(command.eventId(),command.seatNumber());
        seatRepository.save(seat);
        return seat.getId();
    }

    @Override
    public void createSeatsForEvent(CreateSeatsForEventCommand command) {

        List<Seat> seats = new ArrayList<>();

        for(int seatNumber = 1; seatNumber <= command.capacity(); seatNumber++) {
            seats.add(Seat.create(command.eventId(), seatNumber));
        }
        seatRepository.saveAll(seats);
    }

    @Override
    public void release(UUID id) {

        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new SeatNotFoundException(id));
        seat.release();
        seatRepository.save(seat);
    }

    @Override
    public void reserve(UUID id) {
        Seat seat = seatRepository.findByIdWithLock(id)
                .orElseThrow(() -> new SeatNotFoundException(id));
        seat.reserve();
        seatRepository.save(seat);
    }

    @Override
    public void sell(UUID id) {

        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new SeatNotFoundException(id));
        seat.sell();
        seatRepository.save(seat);

    }

    @Override
    public void delete(UUID id) {

        Seat seat = seatRepository.findById(id)
                        .orElseThrow(()-> new SeatNotFoundException(id));

        seatRepository.delete(seat.getId());
    }
}
