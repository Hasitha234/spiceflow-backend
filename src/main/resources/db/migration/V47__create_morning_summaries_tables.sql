CREATE TABLE morning_summaries (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    rep_id BIGINT NOT NULL REFERENCES reps(id),
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    summary_date DATE NOT NULL,
    summary_number VARCHAR(50) NOT NULL UNIQUE,
    final_estimate_value NUMERIC(15, 2) NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255)
);

CREATE INDEX idx_morning_summaries_tenant_id ON morning_summaries(tenant_id);
CREATE INDEX idx_morning_summaries_rep_id ON morning_summaries(rep_id);
CREATE INDEX idx_morning_summaries_driver_id ON morning_summaries(driver_id);
CREATE INDEX idx_morning_summaries_date ON morning_summaries(summary_date);
CREATE INDEX idx_morning_summaries_status ON morning_summaries(status);

CREATE TABLE morning_summary_items (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    morning_summary_id BIGINT NOT NULL REFERENCES morning_summaries(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(15, 2) NOT NULL DEFAULT 0,
    estimate_value NUMERIC(15, 2) NOT NULL DEFAULT 0,
    expected_return_amount INTEGER DEFAULT 0,
    expected_return_price NUMERIC(15, 2) DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255)
);

CREATE INDEX idx_morning_summary_items_tenant_id ON morning_summary_items(tenant_id);
CREATE INDEX idx_morning_summary_items_summary_id ON morning_summary_items(morning_summary_id);
CREATE INDEX idx_morning_summary_items_product_id ON morning_summary_items(product_id);
