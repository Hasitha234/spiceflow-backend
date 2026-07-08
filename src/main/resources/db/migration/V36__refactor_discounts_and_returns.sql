-- V36: Refactor discounts and returns

-- 1. Rep Order changes
ALTER TABLE rep_order_shops ADD COLUMN discount_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE rep_order_shops ADD COLUMN sku_discount_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00;
ALTER TABLE rep_order_shops ADD COLUMN return_warehouse_id BIGINT;
ALTER TABLE rep_order_shops ADD CONSTRAINT fk_rep_order_shop_return_wh FOREIGN KEY (return_warehouse_id) REFERENCES warehouses(id);

ALTER TABLE rep_order_items DROP COLUMN IF EXISTS discount_amount;

-- 2. Purchase changes
ALTER TABLE purchases ADD COLUMN return_warehouse_id BIGINT;
ALTER TABLE purchases ADD CONSTRAINT fk_purchase_return_wh FOREIGN KEY (return_warehouse_id) REFERENCES warehouses(id);

CREATE TABLE purchase_return_items (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    purchase_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    unit_type VARCHAR(10),
    rate DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_purchase_return_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_purchase_return_purchase FOREIGN KEY (purchase_id) REFERENCES purchases(id),
    CONSTRAINT fk_purchase_return_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX idx_purchase_return_items_tenant ON purchase_return_items(tenant_id);
