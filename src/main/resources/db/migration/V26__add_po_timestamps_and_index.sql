-- V26__add_po_timestamps_and_index.sql

ALTER TABLE purchase_orders ADD COLUMN submitted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE purchase_orders ADD COLUMN received_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_po_tenant_status_date ON purchase_orders(tenant_id, status, order_date);
