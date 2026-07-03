-- V18: Create Delivery Tables
-- Handles actual delivery outcomes and shop payments

CREATE TABLE deliveries (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    loading_sheet_id BIGINT NOT NULL REFERENCES loading_sheets(id),
    
    delivery_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    
    total_sales_value NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total_returns_value NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total_collected_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_deliveries_tenant ON deliveries(tenant_id);
CREATE INDEX idx_deliveries_loading ON deliveries(loading_sheet_id);

CREATE TABLE delivery_shops (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    delivery_id BIGINT NOT NULL REFERENCES deliveries(id),
    shop_id BIGINT NOT NULL REFERENCES shops(id),
    
    gross_bill_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total_discount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    returns_deducted NUMERIC(15, 2) NOT NULL DEFAULT 0,
    net_payable NUMERIC(15, 2) NOT NULL DEFAULT 0,
    
    paid_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    credit_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_delivery_shops_tenant ON delivery_shops(tenant_id);
CREATE INDEX idx_delivery_shops_delivery ON delivery_shops(delivery_id);
CREATE INDEX idx_delivery_shops_shop ON delivery_shops(shop_id);

CREATE TABLE delivery_shop_items (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    delivery_shop_id BIGINT NOT NULL REFERENCES delivery_shops(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    
    quantity_delivered INTEGER NOT NULL,
    unit_type VARCHAR(10),
    rate NUMERIC(15, 2) NOT NULL DEFAULT 0,
    
    gross_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    net_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    is_free_item BOOLEAN NOT NULL DEFAULT FALSE,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_delivery_shop_items_tenant ON delivery_shop_items(tenant_id);
CREATE INDEX idx_delivery_shop_items_shop ON delivery_shop_items(delivery_shop_id);

CREATE TABLE delivery_shop_returns (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    delivery_shop_id BIGINT NOT NULL REFERENCES delivery_shops(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    
    quantity_returned INTEGER NOT NULL,
    unit_type VARCHAR(10),
    credit_value NUMERIC(15, 2) NOT NULL DEFAULT 0,
    
    return_type VARCHAR(50) NOT NULL, -- EXPIRED | DAMAGED | GOOD
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_delivery_shop_returns_tenant ON delivery_shop_returns(tenant_id);
CREATE INDEX idx_delivery_shop_returns_shop ON delivery_shop_returns(delivery_shop_id);

CREATE TABLE delivery_payments (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    delivery_shop_id BIGINT NOT NULL REFERENCES delivery_shops(id),
    
    payment_method VARCHAR(50) NOT NULL, -- CASH | CHEQUE | BANK_TRANSFER
    amount NUMERIC(15, 2) NOT NULL,
    
    cheque_no VARCHAR(50),
    cheque_bank_name VARCHAR(100),
    cheque_date DATE,
    
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_delivery_payments_tenant ON delivery_payments(tenant_id);
CREATE INDEX idx_delivery_payments_shop ON delivery_payments(delivery_shop_id);
