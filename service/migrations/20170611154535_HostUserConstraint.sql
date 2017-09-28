
-- +goose Up
-- SQL in section 'Up' is executed when this migration is applied

ALTER TABLE host_users ADD CONSTRAINT unique_pair UNIQUE(host_id, user_id);

-- +goose Down
-- SQL section 'Down' is executed when this migration is rolled back

ALTER TABLE host_users DROP CONSTRAINT unique_pair;
