
-- +goose Up
-- SQL in section 'Up' is executed when this migration is applied

CREATE TABLE hosts (
       host_id                  serial PRIMARY KEY,
       address                  varchar(400) NOT NULL,
       city                     varchar(240) NOT NULL,
       state                    varchar(240) NOT NULL,
       zipcode                  varchar(240) NOT NULL,
       max_occupancy            integer NOT NULL,
       created_at               timestamp DEFAULT current_timestamp,
       updated_at               timestamp DEFAULT current_timestamp
);

-- +goose Down
-- SQL section 'Down' is executed when this migration is rolled back

DROP TABLE hosts;


