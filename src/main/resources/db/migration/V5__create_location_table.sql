-- Create table for Location entity expected by JPA
CREATE TABLE IF NOT EXISTS location (
    id BIGSERIAL PRIMARY KEY,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION
);
