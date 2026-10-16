package com.seatrush.ticket.infrastructure.persistence;

import com.seatrush.ticket.application.query.TicketView;
import com.seatrush.ticket.domain.model.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ObjectFactory;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class TicketMapper {

    public abstract Ticket toEntity(TicketJpaEntity ticketJpaEntity);
    public abstract TicketView toView(TicketJpaEntity ticketJpaEntity);
    public abstract TicketJpaEntity toJpaEntity(Ticket ticket);

    @ObjectFactory
    protected Ticket restoreTicket(TicketJpaEntity ticketJpaEntity) {
        if (ticketJpaEntity == null) return null;
        return Ticket.restore(ticketJpaEntity.getId(),
                ticketJpaEntity.getTicketCode(),
                ticketJpaEntity.getUserId(),
                ticketJpaEntity.getEventId(),
                ticketJpaEntity.getSeatId(),
                ticketJpaEntity.getTicketStatus(),
                ticketJpaEntity.getIssuedAt(),
                ticketJpaEntity.getUsedAt(),
                ticketJpaEntity.getCancelledAt());
    }


}
