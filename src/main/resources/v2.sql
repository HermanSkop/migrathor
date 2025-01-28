-- Add a new column to the 'client' table
ALTER TABLE client
    ADD COLUMN phone_number varchar(20);

-- Modify the 'product' table to allow a nullable 'description' column
ALTER TABLE product
    ALTER COLUMN description DROP NOT NULL;

-- Add a new column to the 'purchase' table for tracking purchase date
ALTER TABLE purchase
    ADD COLUMN purchase_date date;

-- Add a foreign key to 'purchase_item' linking 'product_id' to 'product'
ALTER TABLE purchase_item
    ADD CONSTRAINT fk_product_id
        FOREIGN KEY (product_id)
            REFERENCES product (id)
            ON DELETE CASCADE
            NOT DEFERRABLE
                INITIALLY IMMEDIATE;

-- Add a new 'status' column to 'purchase' table
ALTER TABLE purchase
    ADD COLUMN status varchar(50) DEFAULT 'Pending';

-- Add a new index to the 'purchase_item' table to optimize queries on 'purchase_id'
CREATE INDEX idx_purchase_item_purchase_id
    ON purchase_item (purchase_id);

-- Drop a foreign key constraint from 'purchase_item' (in case you want to test removal)
ALTER TABLE purchase_item
    DROP CONSTRAINT purchase_item_product;
