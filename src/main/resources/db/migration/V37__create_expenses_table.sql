CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(19, 2) NOT NULL,
    category VARCHAR(255) NOT NULL,
    description TEXT,
    date DATE NOT NULL,
    tenant_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    CONSTRAINT fk_expenses_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

CREATE INDEX idx_expenses_tenant_id ON expenses(tenant_id);
CREATE INDEX idx_expenses_date ON expenses(date);
