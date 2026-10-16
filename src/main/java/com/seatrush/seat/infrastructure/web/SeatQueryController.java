package com.seatrush.seat.infrastructure.web;

import com.seatrush.seat.application.port.in.SeatQueryUseCase;
import com.seatrush.seat.application.query.SeatView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class SeatQueryController {

    private final SeatQueryUseCase seatQueryUseCase;
    private final SeatWebMapper seatWebMapper;

    @GetMapping("/seats/{seatId}")
    public SeatResponse getById(
            @PathVariable UUID seatId
    ) {
        SeatView seatView =
                seatQueryUseCase.getById(seatId);

        return seatWebMapper.toResponse(seatView);
    }

    @GetMapping("/events/{eventId}/seats/{seatId}")
    public SeatResponse getByIdAndEventId(
            @PathVariable UUID eventId,
            @PathVariable UUID seatId
    ) {
        SeatView seatView =
                seatQueryUseCase.getByIdAndEventId(
                        seatId,
                        eventId
                );

        return seatWebMapper.toResponse(seatView);
    }

    @GetMapping("/events/{eventId}/seats")
    public EventSeatsResponse getByEventId(
            @PathVariable UUID eventId
    ) {
        List<SeatView> seatViews =
                seatQueryUseCase.getByEventId(eventId);

        List<SeatResponse> seats =
                seatWebMapper.toResponseList(seatViews);

        return new EventSeatsResponse(
                eventId,
                seats.size(),
                seats
        );
    }

    @GetMapping("/seats")
    public SeatListResponse getAll() {

        List<SeatView> seatViews =
                seatQueryUseCase.getAll();

        List<SeatResponse> seats =
                seatWebMapper.toResponseList(seatViews);

        return new SeatListResponse(
                seats.size(),
                seats
        );
    }
}