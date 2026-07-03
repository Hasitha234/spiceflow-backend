-- V13: Enhance Products for Distribution SaaS
-- Adds distribution specific fields like unitType, boxConfiguration, ratePerSoldUnit

ALTER TABLE products ADD COLUMN net_weight VARCHAR(20);
ALTER TABLE products ADD COLUMN unit_type VARCHAR(10);
ALTER TABLE products ADD COLUMN box_configuration VARCHAR(50);
ALTER TABLE products ADD COLUMN items_per_sold_unit INTEGER;
ALTER TABLE products ADD COLUMN sold_units_per_box INTEGER;
ALTER TABLE products ADD COLUMN rate_per_sold_unit NUMERIC(15, 2);
