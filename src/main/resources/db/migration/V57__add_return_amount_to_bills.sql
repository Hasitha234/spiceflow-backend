-- Add return_amount to bills table
ALTER TABLE bills 
ADD COLUMN return_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00;
