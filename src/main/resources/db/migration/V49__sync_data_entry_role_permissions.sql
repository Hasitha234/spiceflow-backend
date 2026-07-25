-- Add missing permissions to existing Data Entry system roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'Data Entry'
  AND r.is_system_role = true
  AND r.deleted_at IS NULL
  AND p.code IN (
    'INVENTORY_VIEW', 'INVENTORY_TRANSFER',
    'PURCHASE_VIEW', 'PURCHASE_CREATE', 'PURCHASE_UPDATE',
    'ORDER_VIEW', 'ORDER_CREATE', 'ORDER_UPDATE',
    'LOADING_VIEW', 'LOADING_CREATE', 'LOADING_CONFIRM',
    'DELIVERY_VIEW', 'DELIVERY_CREATE', 'DELIVERY_UPDATE',
    'SETTINGS_PRODUCTS', 'SETTINGS_SHOPS', 'SETTINGS_REPS',
    'SETTINGS_DRIVERS', 'SETTINGS_SUPPLIERS', 'STORE_VIEW'
  )
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
