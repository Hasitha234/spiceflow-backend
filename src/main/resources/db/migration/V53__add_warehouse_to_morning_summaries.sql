ALTER TABLE morning_summaries ADD COLUMN deducted_warehouse_id BIGINT;
ALTER TABLE morning_summaries ADD CONSTRAINT fk_morning_summaries_warehouse 
    FOREIGN KEY (deducted_warehouse_id) REFERENCES warehouses(id);
