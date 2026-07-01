-- V3: Platform admins are completely separate from tenant users.
-- They have no tenant_id, no plan, no trial — they own the platform itself.

CREATE TABLE platform_admins (
    id            BIGSERIAL    PRIMARY KEY,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name          VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at    TIMESTAMP WITH TIME ZONE,
    CONSTRAINT platform_admins_email_unique UNIQUE (email)
);

CREATE INDEX idx_platform_admins_email ON platform_admins(email);

-- Refresh tokens can belong to either a tenant user OR a platform admin
ALTER TABLE refresh_tokens ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE refresh_tokens ADD COLUMN platform_admin_id BIGINT
    REFERENCES platform_admins(id);

-- Enforce: exactly one of user_id or platform_admin_id must be set
ALTER TABLE refresh_tokens ADD CONSTRAINT refresh_tokens_owner_check
    CHECK (
        (user_id IS NOT NULL AND platform_admin_id IS NULL)
        OR
        (user_id IS NULL AND platform_admin_id IS NOT NULL)
    );
