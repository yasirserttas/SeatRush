package com.seatrush.event.infrastructure.persistence;

import com.seatrush.event.application.port.out.EventRepository;
import com.seatrush.event.domain.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EventRepositoryAdapter implements EventRepository {


    private final SpringDataEventRepository springDataEventRepository;
    private final EventMapper eventMapper;


    @Override
    public Event save(Event event) {
        EventJpaEntity  eventJpaEntity =eventMapper.toJpaEntity(event);

        return eventMapper.fromEntity(springDataEventRepository.save(eventJpaEntity));
    }

    @Override
    public Optional<Event> findById(UUID id) {
        return springDataEventRepository.findById(id).map(eventMapper::fromEntity);
    }

    @Override
    public void delete(UUID id) {

        springDataEventRepository.deleteById(id);

    }
}
