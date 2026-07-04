-- V29__add_rep_orders_workflow_columns.sql
-- Adds workflow FSM governance columns to rep_orders for ADR-013 compliance

ALTER TABLE rep_orders ADD COLUMN IF NOT EXISTS order_number VARCHAR(50);
ALTER TABLE rep_orders ADD COLUMN IF NOT EXISTS status VARCHAR(50) NOT NULL DEFAULT 'DRAFT';
ALTER TABLE rep_orders ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE rep_orders ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);
ALTER TABLE rep_orders ADD COLUMN IF NOT EXISTS approved_by VARCHAR(100);
ALTER TABLE rep_orders ADD COLUMN IF NOT EXISTS loaded_by VARCHAR(100);
ALTER TABLE rep_orders ADD COLUMN IF NOT EXISTS delivered_by VARCHAR(100);
ALTER TABLE rep_orders ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE rep_orders ADD COLUMN IF NOT EXISTS loaded_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE rep_orders ADD COLUMN IF NOT EXISTS delivered_at TIMESTAMP WITH TIME ZONE;

CREATE UNIQUE INDEX IF NOT EXISTS idx_rep_orders_number_tenant ON rep_orders(order_number, tenant_id);
CREATE INDEX IF NOT EXISTS idx_rep_orders_status ON rep_orders(tenant_id, status);
