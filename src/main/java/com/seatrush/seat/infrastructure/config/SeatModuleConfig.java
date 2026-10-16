package com.seatrush.seat.infrastructure.config;

import com.seatrush.seat.SeatReservationApi;
import com.seatrush.seat.application.port.in.SeatCommandUseCase;
import com.seatrush.seat.application.port.in.SeatQueryUseCase;
import com.seatrush.seat.application.port.out.SeatQueryPort;
import com.seatrush.seat.application.port.out.SeatRepository;
import com.seatrush.seat.application.service.SeatCommandService;
import com.seatrush.seat.application.service.SeatQueryService;
import com.seatrush.seat.infrastructure.cache.CacheEvictingSeatCommandUseCase;
import com.seatrush.seat.infrastructure.cache.CachedSeatQueryUseCase;
import com.seatrush.seat.infrastructure.transaction.TransactionalSeatCommandUseCase;
import com.seatrush.seat.infrastructure.transaction.TransactionalSeatQueryUseCase;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeatModuleConfig {

    @Bean
    public CacheEvictingSeatCommandUseCase cacheEvictingSeatCommandUseCase(SeatRepository seatRepository,
                                                                           CacheManager cacheManager) {
        SeatCommandService service = new SeatCommandService(seatRepository);
        TransactionalSeatCommandUseCase transactionalSeatCommandUseCase = new TransactionalSeatCommandUseCase(service);
        return new CacheEvictingSeatCommandUseCase(transactionalSeatCommandUseCase, seatRepository, cacheManager);
    }

    @Bean
    public SeatCommandUseCase seatCommandUseCase(CacheEvictingSeatCommandUseCase cacheEvictingSeatCommandUseCase) {
        return cacheEvictingSeatCommandUseCase;
    }

    @Bean
    public SeatReservationApi seatReservationApi(CacheEvictingSeatCommandUseCase cacheEvictingSeatCommandUseCase) {
        return cacheEvictingSeatCommandUseCase;
    }

    @Bean
    public CachedSeatQueryUseCase cachedSeatQueryUseCase(SeatQueryPort seatQueryPort) {
        SeatQueryService service = new SeatQueryService(seatQueryPort);
        TransactionalSeatQueryUseCase transactionalSeatQueryUseCase = new TransactionalSeatQueryUseCase(service);
        return new CachedSeatQueryUseCase(transactionalSeatQueryUseCase);
    }

    @Bean
    public SeatQueryUseCase seatQueryUseCase(CachedSeatQueryUseCase cachedSeatQueryUseCase) {
        return cachedSeatQueryUseCase;
    }


}