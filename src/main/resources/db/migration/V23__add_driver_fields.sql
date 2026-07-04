-- ===========================================================================
-- Migration: V23__add_driver_fields
-- Description: Adds logistics, licensing, and employment fields to drivers table.
-- Transforms drivers from simple master data into the foundation of logistics.
-- ===========================================================================

ALTER TABLE drivers ADD COLUMN employee_id VARCHAR(50);
ALTER TABLE drivers ADD COLUMN email VARCHAR(100);
ALTER TABLE drivers ADD COLUMN employment_date DATE;
ALTER TABLE drivers ADD COLUMN termination_date DATE;
ALTER TABLE drivers ADD COLUMN license_number VARCHAR(50);
ALTER TABLE drivers ADD COLUMN license_class VARCHAR(50);
ALTER TABLE drivers ADD COLUMN license_expiry DATE;
ALTER TABLE drivers ADD COLUMN default_warehouse_id BIGINT REFERENCES warehouses(id);
ALTER TABLE drivers ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE';

ALTER TABLE drivers RENAME COLUMN vehicle_no TO assigned_vehicle;
ALTER TABLE drivers ALTER COLUMN assigned_vehicle TYPE VARCHAR(100);

CREATE INDEX idx_drivers_default_warehouse ON drivers(default_warehouse_id);
CREATE INDEX idx_drivers_status ON drivers(status);
