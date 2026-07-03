-- V12: Add Store Management enhancements for Delivery SaaS
-- Adds store_type and is_system_store flags to warehouses.

ALTER TABLE warehouses ADD COLUMN store_type VARCHAR(30) NOT NULL DEFAULT 'CUSTOM';
ALTER TABLE warehouses ADD COLUMN is_system_store BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE warehouses ADD COLUMN description VARCHAR(255);
