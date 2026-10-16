package com.seatrush.event.infrastructure.persistence;

import com.seatrush.event.application.query.EventView;
import com.seatrush.event.domain.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ObjectFactory;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public abstract class EventMapper {

    public  abstract  EventJpaEntity toJpaEntity(Event event);

    public  abstract  Event fromEntity(EventJpaEntity jpaEntity);

    public  abstract EventView toView(EventJpaEntity jpaEntity);

    @ObjectFactory
    protected Event restoreEvent(EventJpaEntity jpaEntity) {
        if(jpaEntity == null) return null;

        return Event.restore(jpaEntity.getId(),
                jpaEntity.getTitle(), jpaEntity.getDescription(),
                jpaEntity.getStatus(),jpaEntity.getStartTime(),jpaEntity.getEndTime(), jpaEntity.getTotalCapacity());
    }

}
