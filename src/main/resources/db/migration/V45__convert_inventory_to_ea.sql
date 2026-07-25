-- ===========================================================================
-- Migration: V45__convert_inventory_to_ea
-- Description: Converts existing inventory items and transactions from "Sold Units" (e.g., DZ) to individual items (EA).
-- ===========================================================================

-- Convert inventory_items
UPDATE inventory_items 
SET 
    quantity_available = quantity_available * (
        SELECT CASE 
            WHEN p.unit_type = 'DZ' THEN 12 
            WHEN p.unit_type = 'MC' THEN 1000 
            ELSE 1 
        END 
        FROM products p 
        WHERE p.id = inventory_items.product_id
    ),
    quantity_reserved = quantity_reserved * (
        SELECT CASE 
            WHEN p.unit_type = 'DZ' THEN 12 
            WHEN p.unit_type = 'MC' THEN 1000 
            ELSE 1 
        END 
        FROM products p 
        WHERE p.id = inventory_items.product_id
    );

-- Convert inventory_transactions
UPDATE inventory_transactions 
SET 
    quantity = quantity * (
        SELECT CASE 
            WHEN p.unit_type = 'DZ' THEN 12 
            WHEN p.unit_type = 'MC' THEN 1000 
            ELSE 1 
        END 
        FROM products p 
        JOIN inventory_items ii ON ii.product_id = p.id 
        WHERE ii.id = inventory_transactions.inventory_item_id
    );
