-- V39: Make tenant_id in users table nullable for Business Owners

ALTER TABLE users ALTER COLUMN tenant_id DROP NOT NULL;
