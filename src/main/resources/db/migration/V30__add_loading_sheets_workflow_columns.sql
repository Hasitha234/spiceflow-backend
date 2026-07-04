-- V30__add_loading_sheets_workflow_columns.sql
-- Adds workflow FSM governance columns to loading_sheets for ADR-013 compliance

ALTER TABLE loading_sheets ADD COLUMN IF NOT EXISTS sheet_number VARCHAR(50);
ALTER TABLE loading_sheets ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE loading_sheets ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE loading_sheets ADD COLUMN IF NOT EXISTS confirmed_by VARCHAR(100);
ALTER TABLE loading_sheets ADD COLUMN IF NOT EXISTS cancelled_by VARCHAR(100);
ALTER TABLE loading_sheets ADD COLUMN IF NOT EXISTS confirmed_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE loading_sheets ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP WITH TIME ZONE;

CREATE UNIQUE INDEX IF NOT EXISTS idx_loading_sheets_number_tenant ON loading_sheets(sheet_number, tenant_id);
CREATE INDEX IF NOT EXISTS idx_loading_sheets_status ON loading_sheets(tenant_id, status);
