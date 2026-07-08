-- Shop visits table for QR code verification tracking
CREATE TABLE IF NOT EXISTS shop_visits (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenants(id),
    delivery_id BIGINT REFERENCES deliveries(id),
    shop_id BIGINT NOT NULL REFERENCES shops(id),
    driver_id BIGINT REFERENCES drivers(id),
    visited_at TIMESTAMP WITH TIME ZONE,
    qr_scanned_at TIMESTAMP WITH TIME ZONE,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    notes VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_shop_visits_tenant ON shop_visits(tenant_id);
CREATE INDEX IF NOT EXISTS idx_shop_visits_delivery ON shop_visits(delivery_id);
CREATE INDEX IF NOT EXISTS idx_shop_visits_shop ON shop_visits(shop_id);

COMMENT ON TABLE shop_visits IS 'Tracks QR code verified shop visits during deliveries';
