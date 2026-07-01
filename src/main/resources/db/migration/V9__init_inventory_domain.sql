-- ===========================================================================
-- Migration: V9__init_inventory_domain
-- Description: Creates the core tables for the Inventory and Distribution domain.
-- Includes tables for suppliers, warehouses, product catalog, and stock items.
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- Table: suppliers
-- Description: Stores vendor/supplier information for procurement.
-- ---------------------------------------------------------------------------
CREATE TABLE suppliers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    address TEXT,
    tax_id VARCHAR(100),
    tenant_id BIGINT NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_modified_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_supplier_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

COMMENT ON TABLE suppliers IS 'Stores vendor and supplier information for procurement.';
COMMENT ON COLUMN suppliers.id IS 'Primary key';
COMMENT ON COLUMN suppliers.name IS 'Name of the supplier';
COMMENT ON COLUMN suppliers.tenant_id IS 'Tenant reference for multi-tenancy';

CREATE INDEX idx_suppliers_tenant ON suppliers(tenant_id);

-- ---------------------------------------------------------------------------
-- Table: warehouses
-- Description: Physical or logical locations where inventory is stored.
-- ---------------------------------------------------------------------------
CREATE TABLE warehouses (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location TEXT,
    capacity INT,
    tenant_id BIGINT NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_modified_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_warehouse_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

COMMENT ON TABLE warehouses IS 'Physical or logical locations where inventory is stored.';
COMMENT ON COLUMN warehouses.id IS 'Primary key';
COMMENT ON COLUMN warehouses.name IS 'Name of the warehouse';
COMMENT ON COLUMN warehouses.capacity IS 'Maximum capacity of the warehouse';
COMMENT ON COLUMN warehouses.tenant_id IS 'Tenant reference for multi-tenancy';

CREATE INDEX idx_warehouses_tenant ON warehouses(tenant_id);

-- ---------------------------------------------------------------------------
-- Table: product_categories
-- Description: Hierarchical classification for products.
-- ---------------------------------------------------------------------------
CREATE TABLE product_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parent_category_id BIGINT,
    tenant_id BIGINT NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_modified_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_category_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_category_id) REFERENCES product_categories(id)
);

COMMENT ON TABLE product_categories IS 'Hierarchical classification for products.';
COMMENT ON COLUMN product_categories.id IS 'Primary key';
COMMENT ON COLUMN product_categories.name IS 'Category name';
COMMENT ON COLUMN product_categories.parent_category_id IS 'Reference to parent category if nested';
COMMENT ON COLUMN product_categories.tenant_id IS 'Tenant reference for multi-tenancy';

CREATE INDEX idx_categories_tenant ON product_categories(tenant_id);
CREATE INDEX idx_categories_parent ON product_categories(parent_category_id);

-- ---------------------------------------------------------------------------
-- Table: products
-- Description: The master catalog of all distinct items.
-- ---------------------------------------------------------------------------
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    base_price DECIMAL(10, 2),
    unit_of_measure VARCHAR(50),
    category_id BIGINT,
    supplier_id BIGINT,
    tenant_id BIGINT NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_modified_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_product_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES product_categories(id),
    CONSTRAINT fk_product_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

COMMENT ON TABLE products IS 'The master catalog of all distinct items available.';
COMMENT ON COLUMN products.id IS 'Primary key';
COMMENT ON COLUMN products.sku IS 'Stock Keeping Unit, unique per tenant';
COMMENT ON COLUMN products.name IS 'Name of the product';
COMMENT ON COLUMN products.base_price IS 'Default price of the product';
COMMENT ON COLUMN products.tenant_id IS 'Tenant reference for multi-tenancy';

CREATE UNIQUE INDEX idx_products_sku_tenant ON products(sku, tenant_id);
CREATE INDEX idx_products_tenant ON products(tenant_id);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_supplier ON products(supplier_id);

-- ---------------------------------------------------------------------------
-- Table: inventory_items
-- Description: Tracks actual stock levels of a product in a specific warehouse.
-- ---------------------------------------------------------------------------
CREATE TABLE inventory_items (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    quantity_available INT NOT NULL DEFAULT 0,
    quantity_reserved INT NOT NULL DEFAULT 0,
    batch_number VARCHAR(100),
    expiration_date DATE,
    version BIGINT NOT NULL DEFAULT 0,
    tenant_id BIGINT NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_modified_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_inventory_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
    CONSTRAINT fk_inventory_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

COMMENT ON TABLE inventory_items IS 'Tracks actual stock levels of a product in a specific warehouse.';
COMMENT ON COLUMN inventory_items.id IS 'Primary key';
COMMENT ON COLUMN inventory_items.product_id IS 'Reference to the product';
COMMENT ON COLUMN inventory_items.warehouse_id IS 'Reference to the warehouse';
COMMENT ON COLUMN inventory_items.quantity_available IS 'Quantity physically available';
COMMENT ON COLUMN inventory_items.quantity_reserved IS 'Quantity reserved for pending orders';
COMMENT ON COLUMN inventory_items.tenant_id IS 'Tenant reference for multi-tenancy';

CREATE INDEX idx_inventory_tenant ON inventory_items(tenant_id);
CREATE INDEX idx_inventory_warehouse_product ON inventory_items(warehouse_id, product_id);
CREATE INDEX idx_inventory_product ON inventory_items(product_id);
CREATE INDEX idx_inventory_warehouse ON inventory_items(warehouse_id); -- Added FK Index

-- ---------------------------------------------------------------------------
-- Table: inventory_transactions
-- Description: Immutable ledger of all stock movements and adjustments.
-- ---------------------------------------------------------------------------
CREATE TABLE inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    inventory_item_id BIGINT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL, -- IN, OUT, ADJUST, RESERVE, RELEASE
    quantity INT NOT NULL,
    reference_id VARCHAR(255), -- E.g., Order ID, PO ID
    notes TEXT,
    tenant_id BIGINT NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_modified_by VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_tx_inventory FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id),
    CONSTRAINT fk_tx_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

COMMENT ON TABLE inventory_transactions IS 'Immutable ledger of all stock movements and adjustments.';
COMMENT ON COLUMN inventory_transactions.id IS 'Primary key';
COMMENT ON COLUMN inventory_transactions.inventory_item_id IS 'Reference to the inventory item';
COMMENT ON COLUMN inventory_transactions.transaction_type IS 'Type of movement: IN, OUT, ADJUST, RESERVE, RELEASE';
COMMENT ON COLUMN inventory_transactions.quantity IS 'Amount added or removed';
COMMENT ON COLUMN inventory_transactions.tenant_id IS 'Tenant reference for multi-tenancy';

CREATE INDEX idx_inv_tx_tenant ON inventory_transactions(tenant_id);
CREATE INDEX idx_inv_tx_item ON inventory_transactions(inventory_item_id);
