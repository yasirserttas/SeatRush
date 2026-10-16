package com.seatrush.event.infrastructure.web;

import com.seatrush.event.application.port.in.EventQueryUseCase;
import com.seatrush.event.application.query.EventView;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventQueryController {

    private final EventQueryUseCase eventQueryUseCase;

    @GetMapping("/{id}")
    public EventView getById(
            @PathVariable UUID id
    ) {
        return eventQueryUseCase.getById(id);
    }

    @GetMapping
    public List<EventView> getAll() {
        return eventQueryUseCase.getAll();
    }
}