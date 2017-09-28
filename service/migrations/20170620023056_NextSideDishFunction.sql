-- +goose Up
-- SQL in section 'Up' is executed when this migration is applied
-- +goose StatementBegin

CREATE FUNCTION next_dish(d dish)
RETURNS dish AS $$
BEGIN
  RETURN (CASE WHEN d='main'::dish THEN 'side'::dish
               WHEN d='side'::dish THEN 'appetizer'::dish
               WHEN d='appetizer'::dish THEN 'drinks'::dish
               ELSE 'main'::dish
          END);
END
$$ LANGUAGE plpgsql;
-- +goose StatementEnd
-- +goose Down
-- SQL section 'Down' is executed when this migration is rolled back
drop function next_dish(d dish);
