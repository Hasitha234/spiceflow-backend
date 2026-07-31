ALTER TABLE cancel_summaries ADD COLUMN return_warehouse_id BIGINT REFERENCES warehouses(id);
CREATE INDEX idx_cancel_summaries_return_warehouse ON cancel_summaries(return_warehouse_id);
