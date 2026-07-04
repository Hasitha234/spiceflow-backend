-- V27__create_goods_receipts_and_ledger_tables.sql

CREATE TABLE goods_receipts (
    id BIGSERIAL PRIMARY KEY,
    receipt_number VARCHAR(50) NOT NULL UNIQUE,
    tenant_id BIGINT NOT NULL,
    purchase_order_id BIGINT,
    po_number VARCHAR(50) NOT NULL DEFAULT '',
    supplier_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    receipt_date TIMESTAMP WITH TIME ZONE NOT NULL,
    total_accepted_value DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    total_damaged_value DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    created_by VARCHAR(100) NOT NULL,
    verified_by VARCHAR(100),
    posted_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP WITH TIME ZONE,
    posted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_gr_tenant_status ON goods_receipts(tenant_id, status);
CREATE INDEX idx_gr_receipt_num_tenant ON goods_receipts(receipt_number, tenant_id);
CREATE INDEX idx_gr_po_num ON goods_receipts(po_number);

CREATE TABLE goods_receipt_lines (
    id BIGSERIAL PRIMARY KEY,
    goods_receipt_id BIGINT NOT NULL REFERENCES goods_receipts(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL,
    expected_qty DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    received_qty DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    accepted_qty DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    damaged_qty DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    lot_number VARCHAR(100) NOT NULL DEFAULT '',
    expiration_date DATE,
    unit_price DECIMAL(19,2) NOT NULL,
    line_total DECIMAL(19,2) NOT NULL
);

CREATE INDEX idx_gr_lines_gr_id ON goods_receipt_lines(goods_receipt_id);
CREATE INDEX idx_gr_lines_product_id ON goods_receipt_lines(product_id);

CREATE TABLE inventory_ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    movement_type VARCHAR(30) NOT NULL,
    quantity DECIMAL(19,2) NOT NULL,
    unit_cost DECIMAL(19,2) NOT NULL,
    total_value DECIMAL(19,2) NOT NULL,
    reference_id VARCHAR(50) NOT NULL,
    lot_number VARCHAR(100) NOT NULL DEFAULT '',
    expiration_date DATE,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    performed_by VARCHAR(100) NOT NULL
);

CREATE INDEX idx_ledger_tenant_wh_prod ON inventory_ledger_entries(tenant_id, warehouse_id, product_id);
CREATE INDEX idx_ledger_reference ON inventory_ledger_entries(reference_id);
