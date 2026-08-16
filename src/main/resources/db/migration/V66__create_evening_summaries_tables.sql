-- evening_summaries (parent table)
CREATE TABLE evening_summaries (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    rep_id BIGINT NOT NULL REFERENCES reps(id),
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    summary_date DATE NOT NULL,
    summary_number VARCHAR(50) NOT NULL,
    final_estimate_value NUMERIC(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    inventory_processed BOOLEAN NOT NULL DEFAULT FALSE,
    deduction_warehouse_id BIGINT REFERENCES warehouses(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    UNIQUE(tenant_id, summary_number)
);

-- evening_summary_items (child table)
CREATE TABLE evening_summary_items (
    id BIGSERIAL PRIMARY KEY,
    evening_summary_id BIGINT NOT NULL REFERENCES evening_summaries(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    estimate_value NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255)
);

-- Indexes
CREATE INDEX idx_evening_summaries_tenant_date ON evening_summaries(tenant_id, summary_date);
CREATE INDEX idx_evening_summary_items_summary ON evening_summary_items(evening_summary_id);
