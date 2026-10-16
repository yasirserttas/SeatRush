package com.seatrush.seat.infrastructure.web;

import com.seatrush.seat.application.port.in.SeatCommandUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
public class SeatCommandController {

    private final SeatCommandUseCase seatCommandUseCase;

    @PatchMapping("/{seatId}/reserve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reserve(
            @PathVariable UUID seatId
    ) {
        seatCommandUseCase.reserve(seatId);
    }

    @PatchMapping("/{seatId}/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(
            @PathVariable UUID seatId
    ) {
        seatCommandUseCase.release(seatId);
    }
}