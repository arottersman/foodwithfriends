-- +goose Up
-- SQL in section 'Up' is executed when this migration is applied
ALTER TABLE event_users ADD CONSTRAINT unique_event_user_pair UNIQUE(event_id, user_id);

-- +goose Down
-- SQL section 'Down' is executed when this migration is rolled back
ALTER  TABLE event_users DROP CONSTRAINT unique_event_user_pair;
