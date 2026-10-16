CREATE TABLE event_publication (
    id               UUID                        PRIMARY KEY,
    listener_id      VARCHAR(512)                NOT NULL,
    event_type       VARCHAR(512)                NOT NULL,
    serialized_event VARCHAR(4000)               NOT NULL,
    publication_date TIMESTAMP WITH TIME ZONE   NOT NULL,
    completion_date  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_event_publication_completion_date ON event_publication (completion_date);
