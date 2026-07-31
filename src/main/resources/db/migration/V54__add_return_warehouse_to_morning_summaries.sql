ALTER TABLE morning_summaries
ADD COLUMN return_warehouse_id BIGINT;

ALTER TABLE morning_summaries
ADD CONSTRAINT fk_morning_summaries_return_warehouse
FOREIGN KEY (return_warehouse_id) REFERENCES warehouses(id);
