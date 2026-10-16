package com.seatrush.seat.infrastructure.cache;

import com.seatrush.seat.application.port.in.SeatQueryUseCase;
import com.seatrush.seat.application.query.SeatView;
import com.seatrush.seat.domain.model.SeatStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("CachedSeatQueryUseCase Unit Tests")
class CachedSeatQueryUseCaseTest {

    private final SeatQueryUseCase delegate = mock(SeatQueryUseCase.class);
    private final CachedSeatQueryUseCase cachedUseCase = new CachedSeatQueryUseCase(delegate);

    private SeatView seatView(UUID id) {
        return new SeatView(id, UUID.randomUUID(), 1, SeatStatus.AVAILABLE);
    }

    @Test
    @DisplayName("should delegate getById and return the same view")
    void shouldDelegateGetById() {
        UUID id = UUID.randomUUID();
        SeatView view = seatView(id);
        when(delegate.getById(id)).thenReturn(view);

        SeatView actual = cachedUseCase.getById(id);

        assertThat(actual).isSameAs(view);
        verify(delegate).getById(id);
    }

    @Test
    @DisplayName("should delegate getByIdAndEventId and return the same view")
    void shouldDelegateGetByIdAndEventId() {
        UUID id = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        SeatView view = seatView(id);
        when(delegate.getByIdAndEventId(id, eventId)).thenReturn(view);

        SeatView actual = cachedUseCase.getByIdAndEventId(id, eventId);

        assertThat(actual).isSameAs(view);
        verify(delegate).getByIdAndEventId(id, eventId);
    }

    @Test
    @DisplayName("should delegate getByEventId and return the same list")
    void shouldDelegateGetByEventId() {
        UUID eventId = UUID.randomUUID();
        List<SeatView> views = List.of(seatView(UUID.randomUUID()), seatView(UUID.randomUUID()));
        when(delegate.getByEventId(eventId)).thenReturn(views);

        List<SeatView> actual = cachedUseCase.getByEventId(eventId);

        assertThat(actual).isSameAs(views);
        verify(delegate).getByEventId(eventId);
    }

    @Test
    @DisplayName("should delegate getAll and return the same list")
    void shouldDelegateGetAll() {
        List<SeatView> views = List.of(seatView(UUID.randomUUID()));
        when(delegate.getAll()).thenReturn(views);

        List<SeatView> actual = cachedUseCase.getAll();

        assertThat(actual).isSameAs(views);
        verify(delegate).getAll();
    }

    @Test
    @DisplayName("should propagate delegate exceptions")
    void shouldPropagateDelegateExceptions() {
        UUID id = UUID.randomUUID();
        when(delegate.getById(id)).thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> cachedUseCase.getById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("db down");
    }
}
