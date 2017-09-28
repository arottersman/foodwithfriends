-- +goose Up
-- SQL in section 'Up' is executed when this migration is applied

CREATE TYPE dish AS ENUM (
       'appetizer',
       'side',
       'drinks',
       'main'
);

CREATE TABLE event_users (
       user_id                  serial REFERENCES users ON DELETE CASCADE,
       event_id                 serial REFERENCES events ON DELETE CASCADE,
       assigned_dish            dish,
       bringing                 varchar(240), 
       created_at               timestamp DEFAULT current_timestamp,
       updated_at               timestamp DEFAULT current_timestamp
);

-- +goose Down
-- SQL section 'Down' is executed when this migration is rolled back

DROP TABLE event_users;
DROP TYPE dish;

