-- Create cancel_summaries table
CREATE TABLE cancel_summaries (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    rep_id BIGINT NOT NULL REFERENCES reps(id),
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    summary_date DATE NOT NULL,
    summary_number VARCHAR(50) NOT NULL,
    final_estimate_value DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),

    CONSTRAINT uq_cancel_summary_number UNIQUE (summary_number)
);

CREATE INDEX idx_cancel_summaries_tenant ON cancel_summaries(tenant_id);
CREATE INDEX idx_cancel_summaries_rep ON cancel_summaries(rep_id);
CREATE INDEX idx_cancel_summaries_date ON cancel_summaries(summary_date);
CREATE INDEX idx_cancel_summaries_tenant_rep_date ON cancel_summaries(tenant_id, rep_id, summary_date);

-- Create cancel_summary_items table
CREATE TABLE cancel_summary_items (
    id BIGSERIAL PRIMARY KEY,
    cancel_summary_id BIGINT NOT NULL REFERENCES cancel_summaries(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INT NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    estimate_value DECIMAL(15,2) NOT NULL,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255)
);

CREATE INDEX idx_cancel_summary_items_summary ON cancel_summary_items(cancel_summary_id);
CREATE INDEX idx_cancel_summary_items_product ON cancel_summary_items(product_id);
