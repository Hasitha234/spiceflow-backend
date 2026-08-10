-- V63: Clean up summary items that reference soft-deleted products.
-- These orphaned references cause EntityNotFoundException at runtime
-- because the Product entity uses @SQLRestriction("deleted_at IS NULL").

-- 1. Remove morning summary items referencing soft-deleted products
DELETE FROM morning_summary_items
WHERE product_id IN (SELECT id FROM products WHERE deleted_at IS NOT NULL);

-- 2. Remove cancel summary items referencing soft-deleted products
DELETE FROM cancel_summary_items
WHERE product_id IN (SELECT id FROM products WHERE deleted_at IS NOT NULL);

-- 3. Recalculate final_estimate_value for any affected morning summaries
UPDATE morning_summaries ms
SET final_estimate_value = COALESCE(
    (SELECT SUM(msi.estimate_value) FROM morning_summary_items msi WHERE msi.morning_summary_id = ms.id),
    0
)
WHERE ms.deleted_at IS NULL;

-- 4. Recalculate final_estimate_value for any affected cancel summaries
UPDATE cancel_summaries cs
SET final_estimate_value = COALESCE(
    (SELECT SUM(csi.estimate_value) FROM cancel_summary_items csi WHERE csi.cancel_summary_id = cs.id),
    0
)
WHERE cs.deleted_at IS NULL;
