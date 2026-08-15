-- Add inventory_processed flag to track if stock deduction has happened
ALTER TABLE cancel_summaries ADD COLUMN inventory_processed BOOLEAN NOT NULL DEFAULT FALSE;

-- Backfill: If return_warehouse_id is set, it means the summary was processed under the old logic
-- where stock was added back (or new logic where it's deducted).
UPDATE cancel_summaries 
SET inventory_processed = TRUE 
WHERE return_warehouse_id IS NOT NULL;
