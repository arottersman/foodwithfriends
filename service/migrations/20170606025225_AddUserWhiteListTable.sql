-- +goose Up
-- SQL in section 'Up' is executed when this migration is applied

CREATE TABLE user_whitelist (
       user_whitelist_id        int PRIMARY KEY,
       email                    varchar(240) NOT NULL
);

-- +goose Down
-- SQL section 'Down' is executed when this migration is rolled back

DROP TABLE user_whitelist;

