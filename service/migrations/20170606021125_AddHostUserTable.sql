-- +goose Up
-- SQL in section 'Up' is executed when this migration is applied

CREATE TABLE host_users (
       user_id                  serial REFERENCES users ON DELETE CASCADE,
       host_id                  serial REFERENCES hosts ON DELETE CASCADE,
       created_at               timestamp DEFAULT current_timestamp,
       updated_at               timestamp DEFAULT current_timestamp
);

-- +goose Down
-- SQL section 'Down' is executed when this migration is rolled back

DROP TABLE host_users;

