-- V28__create_warehouse_transfers_tables.sql

CREATE TABLE warehouse_transfers (
    id BIGSERIAL PRIMARY KEY,
    transfer_number VARCHAR(50) NOT NULL UNIQUE,
    tenant_id BIGINT NOT NULL,
    from_warehouse_id BIGINT NOT NULL,
    to_warehouse_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    request_date TIMESTAMP WITH TIME ZONE NOT NULL,
    total_transfer_value DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    created_by VARCHAR(100) NOT NULL,
    approved_by VARCHAR(100),
    shipped_by VARCHAR(100),
    received_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP WITH TIME ZONE,
    shipped_at TIMESTAMP WITH TIME ZONE,
    received_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_wt_tenant_status ON warehouse_transfers(tenant_id, status);
CREATE INDEX idx_wt_transfer_num_tenant ON warehouse_transfers(transfer_number, tenant_id);
CREATE INDEX idx_wt_from_wh ON warehouse_transfers(from_warehouse_id);
CREATE INDEX idx_wt_to_wh ON warehouse_transfers(to_warehouse_id);

CREATE TABLE warehouse_transfer_lines (
    id BIGSERIAL PRIMARY KEY,
    warehouse_transfer_id BIGINT NOT NULL REFERENCES warehouse_transfers(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL,
    requested_qty DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    shipped_qty DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    received_qty DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    damaged_qty DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    lot_number VARCHAR(100) NOT NULL DEFAULT '',
    unit_cost DECIMAL(19,2) NOT NULL,
    line_total DECIMAL(19,2) NOT NULL
);

CREATE INDEX idx_wt_lines_wt_id ON warehouse_transfer_lines(warehouse_transfer_id);
CREATE INDEX idx_wt_lines_product_id ON warehouse_transfer_lines(product_id);
