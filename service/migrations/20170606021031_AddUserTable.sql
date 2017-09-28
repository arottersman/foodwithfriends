
-- +goose Up
-- SQL in section 'Up' is executed when this migration is applied

CREATE TABLE users (
       user_id                  serial PRIMARY KEY,
       name                     varchar(240) NOT NULL,
       email                    varchar(240) NOT NULL,
       dietary_restrictions     varchar(600),
       created_at               timestamp DEFAULT current_timestamp,
       updated_at               timestamp DEFAULT current_timestamp,
       auth0_id                 varchar(400) NOT NULL,
       UNIQUE(auth0_id),
       UNIQUE(email)
);

-- +goose Down
-- SQL section 'Down' is executed when this migration is rolled back

DROP TABLE users;
