package com.seatrush.seat.infrastructure.persistence;

import com.seatrush.seat.application.query.SeatView;
import com.seatrush.seat.domain.model.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ObjectFactory;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class SeatMapper {
    public abstract SeatJpaEntity toJpaEntity(Seat seat);
    public abstract Seat toEntity(SeatJpaEntity seatJpaEntity);
    public abstract SeatView toView(SeatJpaEntity seatJpaEntity);

    @ObjectFactory
    protected Seat restoreSeat(SeatJpaEntity seatJpaEntity) {
        if (seatJpaEntity == null) return null;

        return Seat.restore(seatJpaEntity.getId(),
                seatJpaEntity.getEventId(), seatJpaEntity.getSeatNumber(),seatJpaEntity.getSeatStatus());
    }

}
