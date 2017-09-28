-- +goose Up
-- SQL in this section is executed when the migration is applied.
CREATE TYPE event_creation_status AS ENUM (
       'pending',
       'event_created',
       'pass',
       'complete'
);

CREATE TABLE event_creation_invites (
       host_id                  serial REFERENCES hosts ON DELETE CASCADE,
       status                   event_creation_status,
       sent_at                  timestamp DEFAULT current_timestamp
);

-- +goose Down
-- SQL section 'Down' is executed when this migration is rolled back

DROP TABLE event_creation_invites;
DROP TYPE event_creation_status;

