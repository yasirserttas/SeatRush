package com.seatrush.event.application.port.out;

public interface EventPublisher {
    void publish(Object event);
}
