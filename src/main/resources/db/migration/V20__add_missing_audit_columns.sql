-- V20: Add missing Spring Data JPA auditing columns to all BaseEntity tables
-- created_by / last_modified_by were added to the early tables in V10, but every
-- domain table created in V14-V18 was missing them. Hibernate validate catches
-- this at startup when using a real DB (schema = validate, not create-drop).

-- ── Purchases (V14) ──────────────────────────────────────────────────────────
ALTER TABLE purchases          ADD COLUMN IF NOT EXISTS created_by       VARCHAR(255);
ALTER TABLE purchases          ADD COLUMN IF NOT EXISTS last_modified_by  VARCHAR(255);

-- ── Sales master-data (V15) ──────────────────────────────────────────────────
ALTER TABLE reps               ADD COLUMN IF NOT EXISTS created_by       VARCHAR(255);
ALTER TABLE reps               ADD COLUMN IF NOT EXISTS last_modified_by  VARCHAR(255);

ALTER TABLE drivers            ADD COLUMN IF NOT EXISTS created_by       VARCHAR(255);
ALTER TABLE drivers            ADD COLUMN IF NOT EXISTS last_modified_by  VARCHAR(255);

ALTER TABLE shops              ADD COLUMN IF NOT EXISTS created_by       VARCHAR(255);
ALTER TABLE shops              ADD COLUMN IF NOT EXISTS last_modified_by  VARCHAR(255);

-- ── Rep orders (V16) ─────────────────────────────────────────────────────────
ALTER TABLE rep_orders         ADD COLUMN IF NOT EXISTS created_by       VARCHAR(255);
ALTER TABLE rep_orders         ADD COLUMN IF NOT EXISTS last_modified_by  VARCHAR(255);

-- ── Loading sheets (V17) ─────────────────────────────────────────────────────
ALTER TABLE loading_sheets     ADD COLUMN IF NOT EXISTS created_by       VARCHAR(255);
ALTER TABLE loading_sheets     ADD COLUMN IF NOT EXISTS last_modified_by  VARCHAR(255);

-- ── Deliveries (V18) ─────────────────────────────────────────────────────────
ALTER TABLE deliveries         ADD COLUMN IF NOT EXISTS created_by       VARCHAR(255);
ALTER TABLE deliveries         ADD COLUMN IF NOT EXISTS last_modified_by  VARCHAR(255);
