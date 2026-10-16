package com.seatrush.ticket.infrastructure.web;

import java.util.List;

public record TicketListResponse(
        int totalCount,
        List<TicketResponse> tickets
) {
}
