package com.seatrush.event.infrastructure.persistence;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataEventRepository extends JpaRepository<EventJpaEntity, UUID> {
    Optional<EventJpaEntity> findByTitle(String title);

}
