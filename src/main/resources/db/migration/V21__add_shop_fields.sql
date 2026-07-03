-- V21: Add geospatial coordinates and status to shops table
ALTER TABLE shops ADD COLUMN latitude NUMERIC(10, 7);
ALTER TABLE shops ADD COLUMN longitude NUMERIC(10, 7);
ALTER TABLE shops ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
