-- V11__convert_business_type_to_entity.sql

-- 1. Create the new business_types table
CREATE TABLE business_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- 2. Insert existing Enum values as initial data
INSERT INTO business_types (name, description) VALUES
    ('SPICE', 'Spice manufacturing and distribution'),
    ('BISCUIT', 'Biscuit manufacturing and distribution'),
    ('BAKERY', 'Bakery and pastry items'),
    ('RESTAURANT', 'Restaurant and catering services'),
    ('OTHER', 'Other business types');

-- 3. Add the foreign key column to tenants (nullable for now)
ALTER TABLE tenants ADD COLUMN business_type_id BIGINT;

-- 4. Map existing business_type string to business_types.id
UPDATE tenants t
SET business_type_id = (SELECT id FROM business_types bt WHERE bt.name = t.business_type);

-- If there are any tenants with an unmapped business type, map them to 'OTHER'
UPDATE tenants
SET business_type_id = (SELECT id FROM business_types WHERE name = 'OTHER')
WHERE business_type_id IS NULL;

-- 5. Add constraints to the new column
ALTER TABLE tenants ALTER COLUMN business_type_id SET NOT NULL;
ALTER TABLE tenants ADD CONSTRAINT fk_tenant_business_type FOREIGN KEY (business_type_id) REFERENCES business_types(id);

-- 6. Drop the old string column
ALTER TABLE tenants DROP COLUMN business_type;
