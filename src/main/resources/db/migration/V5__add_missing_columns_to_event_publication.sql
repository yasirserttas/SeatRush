ALTER TABLE event_publication
    ADD COLUMN IF NOT EXISTS completion_attempts    INT                         NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_resubmission_date TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS status                 VARCHAR(50);
