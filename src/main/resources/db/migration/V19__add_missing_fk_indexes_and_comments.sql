-- V19: Add missing foreign key indexes and database comments
-- Explicitly skipping created_at/updated_at indexes as we do not frequently sort/query by these columns

-- 1. tenants
CREATE INDEX IF NOT EXISTS idx_tenants_business_type ON tenants(business_type_id);

-- 2. rep orders
CREATE INDEX IF NOT EXISTS idx_rep_order_items_product ON rep_order_items(product_id);
CREATE INDEX IF NOT EXISTS idx_shop_returns_product ON shop_returns(product_id);

-- 3. loading sheets
CREATE INDEX IF NOT EXISTS idx_loading_sheet_items_product ON loading_sheet_items(product_id);
CREATE INDEX IF NOT EXISTS idx_loading_sheet_returns_product ON loading_sheet_returns(product_id);

-- 4. deliveries
CREATE INDEX IF NOT EXISTS idx_delivery_shop_items_product ON delivery_shop_items(product_id);
CREATE INDEX IF NOT EXISTS idx_delivery_shop_returns_product ON delivery_shop_returns(product_id);

-- 5. Add comments to critical tables
COMMENT ON TABLE tenants IS 'Core multi-tenancy table representing a business entity';
COMMENT ON COLUMN tenants.business_type_id IS 'FK to business_types determining the type of business';
COMMENT ON TABLE users IS 'System users (admin or tenant users)';
COMMENT ON TABLE products IS 'Master product catalog items for a tenant';
COMMENT ON TABLE deliveries IS 'Actual recorded delivery data based on a loading sheet';
