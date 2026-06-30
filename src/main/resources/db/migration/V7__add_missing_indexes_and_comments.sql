-- V7: Add missing performance indexes and enterprise metadata comments

-- ==========================================
-- 1. MISSING PERFORMANCE INDEXES
-- ==========================================

-- Used during tenant registration to prevent duplicate businesses
CREATE INDEX idx_tenants_email ON tenants(email);

-- Used for fast joins when looking up users by their assigned role
CREATE INDEX idx_users_role_id ON users(role_id);

-- Reverse lookup index for the many-to-many join table
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);


-- ==========================================
-- 2. DATABASE METADATA (DATA DICTIONARY)
-- ==========================================
-- This allows Data Analysts and DBAs to understand the schema without reading Java code.

COMMENT ON TABLE tenants IS 'Core table storing all businesses/clients using the SaaS platform.';
COMMENT ON COLUMN tenants.status IS 'ACTIVE, INACTIVE, or SUSPENDED.';
COMMENT ON COLUMN tenants.plan IS 'BASIC, PREMIUM, or ENTERPRISE subscription tier.';
COMMENT ON COLUMN tenants.business_type IS 'e.g. SPICE, DISTRIBUTION, MANUFACTURING';

COMMENT ON TABLE users IS 'Employees belonging to a specific Tenant business.';
COMMENT ON COLUMN users.failed_login_attempts IS 'Tracks brute-force attempts. Locks account at 5.';
COMMENT ON COLUMN users.locked_until IS 'Timestamp until which the account is frozen due to failed logins.';

COMMENT ON TABLE roles IS 'Tenant-specific custom access roles.';
COMMENT ON COLUMN roles.is_system_role IS 'If true, this role (like Owner) cannot be deleted or modified by users.';

COMMENT ON TABLE permissions IS 'System-level access rights. These are hardcoded building blocks assigned to roles.';

COMMENT ON TABLE platform_admins IS 'Super-users who manage the SaaS infrastructure. They have no tenant_id.';
