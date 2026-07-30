-- Fix audit columns for final_balances to match BaseEntity

ALTER TABLE final_balances DROP COLUMN IF EXISTS created_by;
ALTER TABLE final_balances DROP COLUMN IF EXISTS updated_by;

ALTER TABLE final_balances ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE final_balances ADD COLUMN created_by VARCHAR(255);
ALTER TABLE final_balances ADD COLUMN last_modified_by VARCHAR(255);

ALTER TABLE final_balances ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE final_balances ALTER COLUMN updated_at SET NOT NULL;
