-- 1. Add 'SUSPENDED' to the tenant status CHECK constraint
--    We DROP the old constraint and ADD a new one (no data change)
ALTER TABLE tenants DROP CONSTRAINT IF EXISTS tenants_status_check;
ALTER TABLE tenants ADD CONSTRAINT tenants_status_check 
    CHECK (status IN ('TRIAL', 'ACTIVE', 'SUSPENDED', 'LOCKED'));

-- 2. Add user_type column to users table (default = 'TENANT_OWNER' for backward compat)
ALTER TABLE users ADD COLUMN IF NOT EXISTS user_type VARCHAR(30) NOT NULL DEFAULT 'TENANT_OWNER';

-- 3. Create business_owner_tenants join table (many-to-many: one owner -> many tenants)
CREATE TABLE IF NOT EXISTS business_owner_tenants (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users(id),
    tenant_id      BIGINT NOT NULL REFERENCES tenants(id),
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_owner_tenant UNIQUE (user_id, tenant_id)
);
CREATE INDEX IF NOT EXISTS idx_bot_user ON business_owner_tenants(user_id);
CREATE INDEX IF NOT EXISTS idx_bot_tenant ON business_owner_tenants(tenant_id);
