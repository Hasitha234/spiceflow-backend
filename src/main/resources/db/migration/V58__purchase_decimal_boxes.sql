-- Change purchase_line_items columns to DECIMAL to support partial boxes
ALTER TABLE purchase_line_items ALTER COLUMN no_of_boxes TYPE DECIMAL(15,2);
ALTER TABLE purchase_line_items ALTER COLUMN no_of_boxes SET DEFAULT 0.00;

ALTER TABLE purchase_line_items ALTER COLUMN sold_quantity TYPE DECIMAL(15,2);
ALTER TABLE purchase_line_items ALTER COLUMN sold_quantity SET DEFAULT 0.00;

-- Change purchases.total_boxes to DECIMAL to support partial boxes sum
ALTER TABLE purchases ALTER COLUMN total_boxes TYPE DECIMAL(15,2);
ALTER TABLE purchases ALTER COLUMN total_boxes SET DEFAULT 0.00;
