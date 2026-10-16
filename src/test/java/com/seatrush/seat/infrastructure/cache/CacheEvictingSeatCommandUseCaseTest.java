package com.seatrush.seat.infrastructure.cache;

import com.seatrush.seat.application.command.CreateSeatCommand;
import com.seatrush.seat.application.command.CreateSeatsForEventCommand;
import com.seatrush.seat.application.port.in.SeatCommandUseCase;
import com.seatrush.seat.application.port.out.SeatRepository;
import com.seatrush.seat.domain.exception.InvalidSeatStateException;
import com.seatrush.seat.domain.exception.SeatNotFoundException;
import com.seatrush.seat.domain.model.Seat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("CacheEvictingSeatCommandUseCase Unit Tests")
class CacheEvictingSeatCommandUseCaseTest {

    private final SeatCommandUseCase delegate = mock(SeatCommandUseCase.class);
    private final SeatRepository seatRepository = mock(SeatRepository.class);
    private final ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
    private final CacheEvictingSeatCommandUseCase useCase =
            new CacheEvictingSeatCommandUseCase(delegate, seatRepository, cacheManager);

    private final UUID eventId = UUID.randomUUID();
    private final UUID otherEventId = UUID.randomUUID();
    private final Seat seat = Seat.create(eventId, 1);
    private Cache cache;

    @BeforeEach
    void setUp() {
        cache = cacheManager.getCache(CacheEvictingSeatCommandUseCase.EVENT_SEATS_CACHE);
        cache.clear();
        cache.put(eventId, List.of());
        cache.put(otherEventId, List.of());
        when(seatRepository.findById(seat.getId())).thenReturn(Optional.of(seat));
    }

    @Nested
    @DisplayName("Eviction Tests")
    class Eviction {

        @Test
        @DisplayName("should evict only the seat's event cache after reserve")
        void shouldEvictEventCacheAfterReserve() {
            useCase.reserve(seat.getId());

            verify(delegate).reserve(seat.getId());
            assertThat(cache.get(eventId)).isNull();
            assertThat(cache.get(otherEventId)).isNotNull();
        }

        @Test
        @DisplayName("should evict event cache after release")
        void shouldEvictEventCacheAfterRelease() {
            useCase.release(seat.getId());

            assertThat(cache.get(eventId)).isNull();
            assertThat(cache.get(otherEventId)).isNotNull();
        }

        @Test
        @DisplayName("should evict event cache after sell")
        void shouldEvictEventCacheAfterSell() {
            useCase.sell(seat.getId());

            assertThat(cache.get(eventId)).isNull();
            assertThat(cache.get(otherEventId)).isNotNull();
        }

        @Test
        @DisplayName("should evict event cache after delete")
        void shouldEvictEventCacheAfterDelete() {
            useCase.delete(seat.getId());

            assertThat(cache.get(eventId)).isNull();
            assertThat(cache.get(otherEventId)).isNotNull();
        }

        @Test
        @DisplayName("should look up the seat before delegating delete (seat is gone afterwards)")
        void shouldLookUpSeatBeforeDelegatingDelete() {
            InOrder inOrder = inOrder(seatRepository, delegate);

            useCase.delete(seat.getId());

            inOrder.verify(seatRepository).findById(seat.getId());
            inOrder.verify(delegate).delete(seat.getId());
        }

        @Test
        @DisplayName("should evict event cache after creating seats for event")
        void shouldEvictEventCacheAfterCreateSeatsForEvent() {
            useCase.createSeatsForEvent(new CreateSeatsForEventCommand(eventId, 10));

            assertThat(cache.get(eventId)).isNull();
            assertThat(cache.get(otherEventId)).isNotNull();
        }

        @Test
        @DisplayName("should evict event cache and return seat id after create")
        void shouldEvictEventCacheAndReturnIdAfterCreate() {
            CreateSeatCommand command = new CreateSeatCommand(eventId, 7);
            UUID createdId = UUID.randomUUID();
            when(delegate.create(command)).thenReturn(createdId);

            UUID actualId = useCase.create(command);

            assertThat(actualId).isEqualTo(createdId);
            assertThat(cache.get(eventId)).isNull();
            assertThat(cache.get(otherEventId)).isNotNull();
        }
    }

    @Nested
    @DisplayName("Failure Tests")
    class Failure {

        @Test
        @DisplayName("should keep cache when the command fails")
        void shouldKeepCacheWhenCommandFails() {
            doThrow(new InvalidSeatStateException("only available seats can be reserved"))
                    .when(delegate).reserve(seat.getId());

            assertThatThrownBy(() -> useCase.reserve(seat.getId()))
                    .isInstanceOf(InvalidSeatStateException.class);
            assertThat(cache.get(eventId)).isNotNull();
        }

        @Test
        @DisplayName("should propagate SeatNotFoundException and keep cache when seat is missing")
        void shouldPropagateNotFoundAndKeepCacheWhenSeatMissing() {
            UUID missingSeatId = UUID.randomUUID();
            when(seatRepository.findById(missingSeatId)).thenReturn(Optional.empty());
            doThrow(new SeatNotFoundException(missingSeatId))
                    .when(delegate).reserve(missingSeatId);

            assertThatThrownBy(() -> useCase.reserve(missingSeatId))
                    .isInstanceOf(SeatNotFoundException.class);
            assertThat(cache.get(eventId)).isNotNull();
            assertThat(cache.get(otherEventId)).isNotNull();
        }

        @Test
        @DisplayName("should keep cache when createSeatsForEvent fails")
        void shouldKeepCacheWhenCreateSeatsForEventFails() {
            CreateSeatsForEventCommand command = new CreateSeatsForEventCommand(eventId, 10);
            doThrow(new RuntimeException("db down")).when(delegate).createSeatsForEvent(command);

            assertThatThrownBy(() -> useCase.createSeatsForEvent(command))
                    .isInstanceOf(RuntimeException.class);
            assertThat(cache.get(eventId)).isNotNull();
        }
    }

    @Nested
    @DisplayName("Cache Availability Tests")
    class CacheAvailability {

        @Test
        @DisplayName("should not fail when the cache is not configured")
        void shouldNotFailWhenCacheIsNotConfigured() {
            CacheManager emptyCacheManager = mock(CacheManager.class);
            when(emptyCacheManager.getCache(anyString())).thenReturn(null);
            CacheEvictingSeatCommandUseCase useCaseWithoutCache =
                    new CacheEvictingSeatCommandUseCase(delegate, seatRepository, emptyCacheManager);

            useCaseWithoutCache.reserve(seat.getId());

            verify(delegate).reserve(seat.getId());
        }
    }
}
