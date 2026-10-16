package com.seatrush.seat.infrastructure.web;

import java.util.List;

public record SeatListResponse(
        int totalCount,
        List<SeatResponse> seats
) {
}