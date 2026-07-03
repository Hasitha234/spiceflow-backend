-- V16: Create Rep Order Tables
-- Handles order collection from shops by reps and shop returns

CREATE TABLE rep_orders (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    rep_id BIGINT NOT NULL REFERENCES reps(id),
    order_date DATE NOT NULL,
    route_area VARCHAR(100),
    
    total_gross_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total_returns_value NUMERIC(15, 2) NOT NULL DEFAULT 0,
    net_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    
    loading_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_rep_orders_tenant ON rep_orders(tenant_id);
CREATE INDEX idx_rep_orders_rep ON rep_orders(rep_id);

CREATE TABLE rep_order_shops (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    rep_order_id BIGINT NOT NULL REFERENCES rep_orders(id),
    shop_id BIGINT NOT NULL REFERENCES shops(id),
    
    gross_order_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    returns_value NUMERIC(15, 2) NOT NULL DEFAULT 0,
    net_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rep_order_shops_tenant ON rep_order_shops(tenant_id);
CREATE INDEX idx_rep_order_shops_order ON rep_order_shops(rep_order_id);
CREATE INDEX idx_rep_order_shops_shop ON rep_order_shops(shop_id);

CREATE TABLE rep_order_items (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    rep_order_shop_id BIGINT NOT NULL REFERENCES rep_order_shops(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    
    quantity INTEGER NOT NULL,
    unit_type VARCHAR(10),
    rate NUMERIC(15, 2) NOT NULL DEFAULT 0,
    
    gross_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    net_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    is_free_item BOOLEAN NOT NULL DEFAULT FALSE,
    
    boxes_needed INTEGER NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rep_order_items_tenant ON rep_order_items(tenant_id);
CREATE INDEX idx_rep_order_items_shop ON rep_order_items(rep_order_shop_id);

CREATE TABLE shop_returns (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    rep_order_shop_id BIGINT REFERENCES rep_order_shops(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    
    quantity INTEGER NOT NULL,
    unit_type VARCHAR(10),
    credit_value NUMERIC(15, 2) NOT NULL DEFAULT 0,
    
    return_type VARCHAR(50) NOT NULL, -- EXPIRED | DAMAGED
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_shop_returns_tenant ON shop_returns(tenant_id);
CREATE INDEX idx_shop_returns_shop ON shop_returns(rep_order_shop_id);
