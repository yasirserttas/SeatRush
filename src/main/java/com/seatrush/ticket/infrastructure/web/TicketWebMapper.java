package com.seatrush.ticket.infrastructure.web;

import com.seatrush.ticket.application.query.TicketView;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface TicketWebMapper {

    TicketResponse toResponse(TicketView ticketView);

    List<TicketResponse> toResponseList(List<TicketView> ticketViews);
}
