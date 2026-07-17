ALTER TABLE users
ADD COLUMN name VARCHAR(255);

-- Set default name for existing users based on their email prefix
UPDATE users
SET name = SUBSTRING(email FROM 1 FOR POSITION('@' IN email) - 1)
WHERE name IS NULL;

ALTER TABLE users
ALTER COLUMN name SET NOT NULL;
