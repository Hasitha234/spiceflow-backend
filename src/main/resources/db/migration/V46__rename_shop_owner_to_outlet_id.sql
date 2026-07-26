-- V46: Rename owner_name to outlet_id and make it mandatory

-- 1. Rename the column
ALTER TABLE shops RENAME COLUMN owner_name TO outlet_id;

-- 2. Populate null or empty values with an auto-generated Outlet ID
UPDATE shops 
SET outlet_id = 'OUTLET-' || id 
WHERE outlet_id IS NULL OR TRIM(outlet_id) = '';

-- 3. Make the column NOT NULL
ALTER TABLE shops ALTER COLUMN outlet_id SET NOT NULL;
