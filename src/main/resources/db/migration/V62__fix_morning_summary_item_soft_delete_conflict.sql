-- Data recovery for morning summaries
-- 1. Restore the most recent set of soft-deleted items for morning summaries that are completely empty
UPDATE morning_summary_items 
SET deleted_at = NULL 
WHERE morning_summary_id IN (
    SELECT ms.id FROM morning_summaries ms
    WHERE ms.deleted_at IS NULL
    AND NOT EXISTS (
        SELECT 1 FROM morning_summary_items msi 
        WHERE msi.morning_summary_id = ms.id AND msi.deleted_at IS NULL
    )
)
AND deleted_at = (
    SELECT MAX(msi2.deleted_at) 
    FROM morning_summary_items msi2 
    WHERE msi2.morning_summary_id = morning_summary_items.morning_summary_id
);

-- 2. Remove items that reference soft-deleted products (these would cause EntityNotFoundException)
DELETE FROM morning_summary_items
WHERE product_id IN (SELECT id FROM products WHERE deleted_at IS NOT NULL);

-- 3. Hard-delete all remaining soft-deleted item rows (since we are moving to hard deletes for child items)
DELETE FROM morning_summary_items WHERE deleted_at IS NOT NULL;

-- 3. Data recovery for cancel summaries (similar logic)
UPDATE cancel_summary_items 
SET deleted_at = NULL 
WHERE cancel_summary_id IN (
    SELECT cs.id FROM cancel_summaries cs
    WHERE cs.deleted_at IS NULL
    AND NOT EXISTS (
        SELECT 1 FROM cancel_summary_items csi 
        WHERE csi.cancel_summary_id = cs.id AND csi.deleted_at IS NULL
    )
)
AND deleted_at = (
    SELECT MAX(csi2.deleted_at) 
    FROM cancel_summary_items csi2 
    WHERE csi2.cancel_summary_id = cancel_summary_items.cancel_summary_id
);

-- Remove cancel summary items that reference soft-deleted products
DELETE FROM cancel_summary_items
WHERE product_id IN (SELECT id FROM products WHERE deleted_at IS NOT NULL);

DELETE FROM cancel_summary_items WHERE deleted_at IS NOT NULL;

-- 4. Add unique index for morning_summary_items to prevent duplicate products
-- Ensure any existing duplicates are merged or removed first (this is a cleanup step if needed)
-- We'll assume the application logic prevents this now, but to add the index safely:
-- Keep only the first one if there are duplicates:
DELETE FROM morning_summary_items a USING (
      SELECT MIN(id) as id, morning_summary_id, product_id
      FROM morning_summary_items 
      GROUP BY morning_summary_id, product_id HAVING COUNT(*) > 1
  ) b
  WHERE a.morning_summary_id = b.morning_summary_id 
  AND a.product_id = b.product_id 
  AND a.id <> b.id;

CREATE UNIQUE INDEX uq_morning_summary_items_product 
ON morning_summary_items(morning_summary_id, product_id);

-- 5. Add unique index for cancel_summary_items
DELETE FROM cancel_summary_items a USING (
      SELECT MIN(id) as id, cancel_summary_id, product_id
      FROM cancel_summary_items 
      GROUP BY cancel_summary_id, product_id HAVING COUNT(*) > 1
  ) b
  WHERE a.cancel_summary_id = b.cancel_summary_id 
  AND a.product_id = b.product_id 
  AND a.id <> b.id;

CREATE UNIQUE INDEX uq_cancel_summary_items_product 
ON cancel_summary_items(cancel_summary_id, product_id);
