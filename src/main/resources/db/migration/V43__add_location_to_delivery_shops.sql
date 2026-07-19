ALTER TABLE delivery_shops ADD COLUMN latitude DOUBLE PRECISION;
ALTER TABLE delivery_shops ADD COLUMN longitude DOUBLE PRECISION;
ALTER TABLE delivery_shops ADD COLUMN location_accuracy DOUBLE PRECISION;
ALTER TABLE delivery_shops ADD COLUMN location_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE delivery_shops ADD COLUMN distance_from_shop DOUBLE PRECISION;
ALTER TABLE delivery_shops ADD COLUMN notes VARCHAR(500);

COMMENT ON COLUMN delivery_shops.distance_from_shop IS 'Server-calculated distance in meters between driver GPS and shop stored coordinates';
COMMENT ON COLUMN delivery_shops.location_verified IS 'True if driver was within acceptable radius of shop';
