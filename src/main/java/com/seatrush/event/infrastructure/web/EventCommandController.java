package com.seatrush.event.infrastructure.web;

import com.seatrush.event.application.command.CreateEventCommand;
import com.seatrush.event.application.command.UpdateEventCommand;
import com.seatrush.event.application.port.in.EventCommandUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventCommandController {

    private final EventCommandUseCase eventCommandUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateEventResponse create(
            @Valid @RequestBody CreateEventRequest request
    ) {
        UUID eventId = eventCommandUseCase.create(
                new CreateEventCommand(
                        request.title(),
                        request.description(),
                        request.startTime(),
                        request.endTime(),
                        request.totalCapacity()
                )
        );

        return new CreateEventResponse(eventId);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request
    ) {
        eventCommandUseCase.update(
                new UpdateEventCommand(
                        id,
                        request.title(),
                        request.description(),
                        request.startTime(),
                        request.endTime(),
                        request.totalCapacity()
                )
        );
    }

    @PatchMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(
            @PathVariable UUID id
    ) {
        eventCommandUseCase.cancel(id);
    }

    @PatchMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(
            @PathVariable UUID id
    ) {
        eventCommandUseCase.activate(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id
    ) {
        eventCommandUseCase.delete(id);
    }
}