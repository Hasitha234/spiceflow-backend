-- V25__create_purchase_orders_tables.sql

CREATE TABLE purchase_orders (
    id BIGSERIAL PRIMARY KEY,
    correlation_id VARCHAR(50) NOT NULL UNIQUE,
    tenant_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    order_date TIMESTAMP WITH TIME ZONE NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    created_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_po_tenant_status ON purchase_orders(tenant_id, status);
CREATE INDEX idx_po_correlation_tenant ON purchase_orders(correlation_id, tenant_id);

CREATE TABLE purchase_order_lines (
    id BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL,
    quantity DECIMAL(19,2) NOT NULL,
    unit_price DECIMAL(19,2) NOT NULL,
    line_total DECIMAL(19,2) NOT NULL
);

CREATE INDEX idx_po_lines_po_id ON purchase_order_lines(purchase_order_id);

ALTER TABLE audit_entries ADD COLUMN aggregate_id VARCHAR(100);

CREATE INDEX idx_audit_aggregate ON audit_entries(tenant_id, aggregate_id);
