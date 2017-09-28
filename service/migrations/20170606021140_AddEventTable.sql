
-- +goose Up
-- SQL in section 'Up' is executed when this migration is applied

CREATE TABLE events (
       event_id                 serial PRIMARY KEY,
       host_id                  serial REFERENCES hosts ON DELETE CASCADE,
       title                    varchar(240) NOT NULL,
       description              varchar(600),
       happening_at             timestamp NOT NULL,
       created_at               timestamp DEFAULT current_timestamp,
       updated_at               timestamp DEFAULT current_timestamp
);

-- +goose Down
-- SQL section 'Down' is executed when this migration is rolled back

DROP TABLE events;


