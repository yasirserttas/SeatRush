package com.seatrush.event.infrastructure.persistence;

import com.seatrush.event.application.port.out.EventQueryPort;
import com.seatrush.event.application.query.EventView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EventQueryAdapter implements EventQueryPort {

    private final SpringDataEventRepository springDataEventRepository;
    private final EventMapper eventMapper;


    @Override
    public Optional<EventView> findById(UUID id) {
        return springDataEventRepository.findById(id).map(eventMapper::toView);
    }

    @Override
    public Optional<EventView> findByTitle(String title) {
        return springDataEventRepository.findByTitle(title).map(eventMapper::toView);
    }

    @Override
    public List<EventView> findAll() {
        return springDataEventRepository.findAll().stream().map(eventMapper::toView).toList();
    }
}
