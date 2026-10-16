CREATE TABLE tickets (
    id            UUID                        PRIMARY KEY,
    ticket_code   VARCHAR(255)                NOT NULL,
    user_id       UUID                        NOT NULL,
    seat_id       UUID                        NOT NULL,
    event_id      UUID                        NOT NULL,
    ticket_status VARCHAR(50)                 NOT NULL,
    issued_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    used_at       TIMESTAMP WITHOUT TIME ZONE,
    cancelled_at  TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT uk_ticket_code  UNIQUE (ticket_code),
    CONSTRAINT fk_ticket_user  FOREIGN KEY (user_id)  REFERENCES users(id),
    CONSTRAINT fk_ticket_seat  FOREIGN KEY (seat_id)  REFERENCES seats(id),
    CONSTRAINT fk_ticket_event FOREIGN KEY (event_id) REFERENCES events(id)
);
