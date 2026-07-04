-- V31__add_deliveries_workflow_columns.sql
-- Adds workflow FSM governance columns to deliveries for ADR-013 compliance

ALTER TABLE deliveries ADD COLUMN IF NOT EXISTS delivery_number VARCHAR(50);
ALTER TABLE deliveries ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE deliveries ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE deliveries ADD COLUMN IF NOT EXISTS dispatched_by VARCHAR(100);
ALTER TABLE deliveries ADD COLUMN IF NOT EXISTS completed_by VARCHAR(100);
ALTER TABLE deliveries ADD COLUMN IF NOT EXISTS cancelled_by VARCHAR(100);
ALTER TABLE deliveries ADD COLUMN IF NOT EXISTS dispatched_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE deliveries ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE deliveries ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP WITH TIME ZONE;

-- Back-fill delivery_number for any existing rows
UPDATE deliveries SET delivery_number = CONCAT('DEL-', id) WHERE delivery_number IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_deliveries_number_tenant ON deliveries(delivery_number, tenant_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_status_tenant ON deliveries(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_deliveries_date_tenant ON deliveries(tenant_id, delivery_date);
