CREATE TABLE seats (
    id          UUID        PRIMARY KEY,
    event_id    UUID        NOT NULL,
    seat_number INT         NOT NULL,
    seat_status VARCHAR(50) NOT NULL,

    CONSTRAINT fk_seat_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT uk_event_seat_number UNIQUE (event_id, seat_number)
);

CREATE INDEX idx_seat_event_id ON seats (event_id);
CREATE INDEX idx_seat_status   ON seats (seat_status);
