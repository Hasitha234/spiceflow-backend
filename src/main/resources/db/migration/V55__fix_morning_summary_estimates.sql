-- ===========================================================================
-- Migration: V55__fix_morning_summary_estimates
-- Description: Recalculates historical morning summary estimates to use base_price 
--              as a fallback when rate_per_sold_unit is 0 or null, healing
--              data corrupted by a backend calculation bug.
-- ===========================================================================

-- 1. Update items with correct unit price and estimate value
UPDATE morning_summary_items 
SET 
    unit_price = (
        SELECT COALESCE(NULLIF(p.rate_per_sold_unit, 0), p.base_price, 0)
        FROM products p
        WHERE p.id = morning_summary_items.product_id
    ),
    estimate_value = quantity * (
        SELECT COALESCE(NULLIF(p.rate_per_sold_unit, 0), p.base_price, 0)
        FROM products p
        WHERE p.id = morning_summary_items.product_id
    )
WHERE (unit_price IS NULL OR unit_price = 0) 
  AND (
        SELECT COALESCE(NULLIF(p.rate_per_sold_unit, 0), p.base_price, 0)
        FROM products p
        WHERE p.id = morning_summary_items.product_id
  ) > 0;

-- 2. Recalculate totals for morning summaries
UPDATE morning_summaries ms
SET final_estimate_value = (
    SELECT COALESCE(SUM(msi.estimate_value), 0)
    FROM morning_summary_items msi
    WHERE msi.morning_summary_id = ms.id
);
