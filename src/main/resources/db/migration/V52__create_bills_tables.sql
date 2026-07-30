-- Create bills table
CREATE TABLE bills (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    rep_id BIGINT NOT NULL REFERENCES reps(id),
    driver_id BIGINT REFERENCES drivers(id),
    shop_id BIGINT NOT NULL REFERENCES shops(id),
    bill_number VARCHAR(50) NOT NULL,
    bill_date DATE NOT NULL,
    
    net_total DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    reverse_grts DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    free_items_value DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    discount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    sku_discount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    final_total DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    cash_collected DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    check_collected DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    loan_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    loan_due_date DATE,
    loan_status VARCHAR(20) NOT NULL DEFAULT 'NONE',
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),

    CONSTRAINT uq_bills_number UNIQUE (tenant_id, bill_number),
    CONSTRAINT uq_bills_shop_date UNIQUE (tenant_id, shop_id, bill_date)
);

CREATE INDEX idx_bills_tenant ON bills(tenant_id);
CREATE INDEX idx_bills_rep ON bills(rep_id);
CREATE INDEX idx_bills_shop ON bills(shop_id);
CREATE INDEX idx_bills_date ON bills(bill_date);
CREATE INDEX idx_bills_tenant_rep_date ON bills(tenant_id, rep_id, bill_date);
