-- V14: Create Purchase Module Tables
-- Handles supplier invoices, multiple payment methods, and stock updates

CREATE TABLE purchases (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    supplier_id BIGINT NOT NULL REFERENCES suppliers(id),
    invoice_no VARCHAR(100) NOT NULL,
    invoice_date DATE NOT NULL,
    order_no VARCHAR(100),
    lc_no VARCHAR(100),
    
    total_boxes INTEGER NOT NULL DEFAULT 0,
    gross_weight_kg NUMERIC(10, 2),
    
    total_order_value NUMERIC(15, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    returns_deducted_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    
    value_of_supply NUMERIC(15, 2) NOT NULL DEFAULT 0,
    vat_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    net_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    
    payment_method VARCHAR(50) NOT NULL,
    cheque_no VARCHAR(100),
    cheque_bank_name VARCHAR(100),
    cheque_amount NUMERIC(15, 2),
    
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    notes TEXT,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_purchases_tenant ON purchases(tenant_id);
CREATE INDEX idx_purchases_supplier ON purchases(supplier_id);
CREATE INDEX idx_purchases_invoice_no ON purchases(invoice_no);

CREATE TABLE purchase_line_items (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    purchase_id BIGINT NOT NULL REFERENCES purchases(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    
    no_of_boxes INTEGER NOT NULL DEFAULT 0,
    sold_quantity INTEGER NOT NULL DEFAULT 0,
    unit_type VARCHAR(10),
    rate NUMERIC(15, 2) NOT NULL DEFAULT 0,
    amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_purchase_lines_tenant ON purchase_line_items(tenant_id);
CREATE INDEX idx_purchase_lines_purchase ON purchase_line_items(purchase_id);
CREATE INDEX idx_purchase_lines_product ON purchase_line_items(product_id);
