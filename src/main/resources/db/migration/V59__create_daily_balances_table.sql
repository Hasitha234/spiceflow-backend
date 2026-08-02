CREATE TABLE IF NOT EXISTS daily_balances (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    balance_date DATE NOT NULL,
    morning_summary_total DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    cancel_summary_total DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    net_dispatch_total DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    bills_total DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),

    CONSTRAINT uq_daily_balance_date UNIQUE (tenant_id, balance_date)
);

CREATE INDEX idx_daily_balances_tenant_date ON daily_balances(tenant_id, balance_date);
CREATE INDEX idx_daily_balances_deleted_at ON daily_balances(deleted_at);
