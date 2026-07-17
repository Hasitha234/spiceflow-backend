DELETE FROM "flyway_schema_history" WHERE "version" = '42';
ALTER TABLE users DROP COLUMN IF EXISTS name;
