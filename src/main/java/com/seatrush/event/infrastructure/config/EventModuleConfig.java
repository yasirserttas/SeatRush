package com.seatrush.event.infrastructure.config;

import com.seatrush.event.application.port.in.EventCommandUseCase;
import com.seatrush.event.application.port.in.EventQueryUseCase;
import com.seatrush.event.application.port.out.EventPublisher;
import com.seatrush.event.application.port.out.EventQueryPort;
import com.seatrush.event.application.port.out.EventRepository;
import com.seatrush.event.application.service.EventCommandService;
import com.seatrush.event.application.service.EventQueryService;
import com.seatrush.event.infrastructure.transaction.TransactionalEventCommandUseCase;
import com.seatrush.event.infrastructure.transaction.TransactionalEventQueryUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventModuleConfig {

    @Bean
    public EventCommandUseCase eventCommandUseCase(
            EventRepository eventRepository,
            EventPublisher eventPublisher
    ) {
        EventCommandService service =
                new EventCommandService(eventRepository, eventPublisher);

        return new TransactionalEventCommandUseCase(service);
    }

    @Bean
    public EventQueryUseCase eventQueryUseCase(
            EventQueryPort eventQueryPort
    ) {
        EventQueryService service =
                new EventQueryService(eventQueryPort);

        return new TransactionalEventQueryUseCase(service);
    }
}