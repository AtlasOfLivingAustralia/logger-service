-- Add remote_address table missing from V1__init.sql

CREATE TABLE IF NOT EXISTS remote_address
(
    id        SERIAL PRIMARY KEY,
    ip        varchar(255) NOT NULL,
    host_name varchar(255) NOT NULL
);

