package com.seatrush.seat.infrastructure.web;

import com.seatrush.seat.application.query.SeatView;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface SeatWebMapper {

    SeatResponse toResponse(SeatView seatView);

    List<SeatResponse> toResponseList(List<SeatView> seatViews);
}