-- Recalculate unit_price and estimate_value for all cancel_summary_items where prices are zero.
-- Uses the same logic as resolveUnitPrice(): prefer rate_per_sold_unit if > 0, else base_price if > 0, else 0.
-- Also recalculates cancel_summaries.final_estimate_value as the sum of all item estimate_values.

-- Step 1: Fix cancel_summary_items with zero prices
UPDATE cancel_summary_items csi
SET unit_price = CASE
        WHEN p.rate_per_sold_unit IS NOT NULL AND p.rate_per_sold_unit > 0 THEN p.rate_per_sold_unit
        WHEN p.base_price IS NOT NULL AND p.base_price > 0 THEN p.base_price
        ELSE 0
    END,
    estimate_value = CASE
        WHEN p.rate_per_sold_unit IS NOT NULL AND p.rate_per_sold_unit > 0 THEN p.rate_per_sold_unit * csi.quantity
        WHEN p.base_price IS NOT NULL AND p.base_price > 0 THEN p.base_price * csi.quantity
        ELSE 0
    END
FROM products p
WHERE csi.product_id = p.id
  AND (csi.unit_price = 0 OR csi.unit_price IS NULL);

-- Step 2: Recalculate final_estimate_value for affected cancel_summaries
UPDATE cancel_summaries cs
SET final_estimate_value = sub.total
FROM (
    SELECT cancel_summary_id, COALESCE(SUM(estimate_value), 0) AS total
    FROM cancel_summary_items
    WHERE deleted_at IS NULL
    GROUP BY cancel_summary_id
) sub
WHERE cs.id = sub.cancel_summary_id
  AND (cs.final_estimate_value = 0 OR cs.final_estimate_value IS NULL);
