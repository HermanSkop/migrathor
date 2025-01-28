-- Undo: Remove the 'phone_number' column from the 'client' table
ALTER TABLE client
    DROP COLUMN phone_number;

-- Undo: Revert the 'description' column in 'product' table to NOT NULL
ALTER TABLE product
    ALTER COLUMN description SET NOT NULL;

-- Undo: Remove the 'purchase_date' column from the 'purchase' table
ALTER TABLE purchase
    DROP COLUMN purchase_date;

-- Undo: Remove the foreign key constraint 'fk_product_id' from 'order_item' table
ALTER TABLE purchase_item
    DROP CONSTRAINT fk_product_id;

-- Undo: Remove the 'status' column from the 'purchase' table
ALTER TABLE purchase
    DROP COLUMN status;

-- Undo: Drop the index 'idx_purchase_item_purchase_id' from the 'order_item' table
DROP INDEX IF EXISTS idx_purchase_item_purchase_id;

-- Undo: Re-add the dropped foreign key constraint to 'purchase_item'
ALTER TABLE purchase_item
    ADD CONSTRAINT purchase_item_product
        FOREIGN KEY (product_id)
            REFERENCES product (id)
            NOT DEFERRABLE
                INITIALLY IMMEDIATE;
