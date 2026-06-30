-- V5: Role-Based Access Control (RBAC) Engine
-- Replaces hardcoded role strings with a flexible permissions system.

-- ─── 1. PERMISSIONS (System-defined, shared across all tenants) ─────────────

CREATE TABLE permissions (
    id          BIGSERIAL    PRIMARY KEY,
    code        VARCHAR(100) NOT NULL,
    module      VARCHAR(50)  NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT permissions_code_unique UNIQUE (code)
);

-- ─── 2. ROLES (Per-tenant — each tenant defines their own roles) ────────────

CREATE TABLE roles (
    id             BIGSERIAL    PRIMARY KEY,
    tenant_id      BIGINT       NOT NULL REFERENCES tenants(id),
    name           VARCHAR(100) NOT NULL,
    description    VARCHAR(255),
    is_system_role BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at     TIMESTAMP WITH TIME ZONE,
    CONSTRAINT roles_tenant_name_unique UNIQUE (tenant_id, name)
);

CREATE INDEX idx_roles_tenant ON roles(tenant_id);

-- ─── 3. ROLE ↔ PERMISSION (Many-to-Many join table) ────────────────────────

CREATE TABLE role_permissions (
    role_id       BIGINT NOT NULL REFERENCES roles(id),
    permission_id BIGINT NOT NULL REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);

-- ─── 4. UPDATE USERS TABLE ─────────────────────────────────────────────────
-- Add role_id FK column. Keep old 'role' column temporarily for migration.

ALTER TABLE users ADD COLUMN role_id BIGINT REFERENCES roles(id);

-- ─── 5. SEED SYSTEM PERMISSIONS ────────────────────────────────────────────
-- These are the building blocks. Tenants assign these to their custom roles.

INSERT INTO permissions (code, module, description) VALUES
    -- Dashboard
    ('DASHBOARD_VIEW',          'DASHBOARD',    'View the main dashboard'),

    -- Purchase Module
    ('PURCHASE_VIEW',           'PURCHASE',     'View purchase invoices'),
    ('PURCHASE_CREATE',         'PURCHASE',     'Create new purchase entries'),
    ('PURCHASE_UPDATE',         'PURCHASE',     'Edit existing purchases'),
    ('PURCHASE_DELETE',         'PURCHASE',     'Delete purchase records'),

    -- Inventory Module
    ('INVENTORY_VIEW',          'INVENTORY',    'View stock levels across stores'),
    ('INVENTORY_TRANSFER',      'INVENTORY',    'Transfer stock between stores'),

    -- Store Management
    ('STORE_VIEW',              'STORE',        'View store details'),
    ('STORE_CREATE',            'STORE',        'Create new stores'),
    ('STORE_UPDATE',            'STORE',        'Update store settings'),
    ('STORE_DELETE',            'STORE',        'Delete/deactivate stores'),

    -- Order Module
    ('ORDER_VIEW',              'ORDER',        'View rep orders'),
    ('ORDER_CREATE',            'ORDER',        'Create new rep order bills'),
    ('ORDER_UPDATE',            'ORDER',        'Edit existing orders'),
    ('ORDER_DELETE',            'ORDER',        'Delete order records'),

    -- Loading Module
    ('LOADING_VIEW',            'LOADING',      'View loading sheets'),
    ('LOADING_CREATE',          'LOADING',      'Generate loading sheets'),
    ('LOADING_CONFIRM',         'LOADING',      'Confirm lorry loading'),

    -- Delivery Module
    ('DELIVERY_VIEW',           'DELIVERY',     'View delivery records'),
    ('DELIVERY_CREATE',         'DELIVERY',     'Record shop deliveries'),
    ('DELIVERY_UPDATE',         'DELIVERY',     'Edit delivery records'),

    -- Payment Module
    ('PAYMENT_VIEW',            'PAYMENT',      'View payment records'),
    ('PAYMENT_CREATE',          'PAYMENT',      'Record payments'),
    ('PAYMENT_UPDATE',          'PAYMENT',      'Edit payment records'),
    ('LOAN_VIEW',               'PAYMENT',      'View loan balances'),
    ('LOAN_MANAGE',             'PAYMENT',      'Manage shop loans'),

    -- Returns Module
    ('RETURNS_VIEW',            'RETURNS',      'View returns records'),
    ('RETURNS_CREATE',          'RETURNS',      'Record shop returns'),
    ('RETURNS_PROCESS',         'RETURNS',      'Process returns to supplier'),

    -- Reporting Module
    ('REPORT_DAILY',            'REPORTS',      'View end of day report'),
    ('REPORT_MONTHLY',          'REPORTS',      'View monthly P&L and reports'),
    ('REPORT_EXPORT',           'REPORTS',      'Export reports as PDF'),

    -- Settings / Master Data
    ('SETTINGS_PRODUCTS',       'SETTINGS',     'Manage products master data'),
    ('SETTINGS_SHOPS',          'SETTINGS',     'Manage shops master data'),
    ('SETTINGS_REPS',           'SETTINGS',     'Manage sales reps'),
    ('SETTINGS_DRIVERS',        'SETTINGS',     'Manage drivers'),
    ('SETTINGS_SUPPLIERS',      'SETTINGS',     'Manage suppliers'),
    ('SETTINGS_PAYMENT_TYPES',  'SETTINGS',     'Manage payment types'),

    -- User & Role Management
    ('USER_VIEW',               'USER_MGMT',    'View users list'),
    ('USER_CREATE',             'USER_MGMT',    'Create new users'),
    ('USER_UPDATE',             'USER_MGMT',    'Edit user details'),
    ('USER_DELETE',             'USER_MGMT',    'Deactivate users'),
    ('ROLE_VIEW',               'USER_MGMT',    'View roles'),
    ('ROLE_CREATE',             'USER_MGMT',    'Create custom roles'),
    ('ROLE_UPDATE',             'USER_MGMT',    'Edit roles and permissions'),
    ('ROLE_DELETE',             'USER_MGMT',    'Delete roles');
