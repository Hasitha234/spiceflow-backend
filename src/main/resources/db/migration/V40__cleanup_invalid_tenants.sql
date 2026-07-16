-- Fix: Clear tenant_id for PLATFORM_ADMIN and TENANT_OWNER since they don't belong to a single tenant
UPDATE users 
SET tenant_id = NULL 
WHERE user_type IN ('PLATFORM_ADMIN', 'TENANT_OWNER');

-- Fix: Any orphaned tenant_ids (pointing to deleted/non-existent tenants) should be cleared
UPDATE users
SET tenant_id = NULL
WHERE tenant_id IS NOT NULL 
  AND tenant_id NOT IN (SELECT id FROM tenants WHERE deleted_at IS NULL);
