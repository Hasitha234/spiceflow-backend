-- V17: Create Loading Tables
-- Handles loading of items onto lorries based on rep orders

CREATE TABLE loading_sheets (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    rep_order_id BIGINT NOT NULL REFERENCES rep_orders(id),
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    
    loading_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_loading_sheets_tenant ON loading_sheets(tenant_id);
CREATE INDEX idx_loading_sheets_rep_order ON loading_sheets(rep_order_id);

CREATE TABLE loading_sheet_items (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    loading_sheet_id BIGINT NOT NULL REFERENCES loading_sheets(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    
    quantity_loaded INTEGER NOT NULL DEFAULT 0,
    unit_type VARCHAR(10),
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_loading_sheet_items_tenant ON loading_sheet_items(tenant_id);
CREATE INDEX idx_loading_sheet_items_sheet ON loading_sheet_items(loading_sheet_id);

CREATE TABLE loading_sheet_returns (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    loading_sheet_id BIGINT NOT NULL REFERENCES loading_sheets(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    
    quantity_returned INTEGER NOT NULL DEFAULT 0,
    unit_type VARCHAR(10),
    return_type VARCHAR(50) NOT NULL, -- EXPIRED | DAMAGED | GOOD
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_loading_sheet_returns_tenant ON loading_sheet_returns(tenant_id);
CREATE INDEX idx_loading_sheet_returns_sheet ON loading_sheet_returns(loading_sheet_id);
