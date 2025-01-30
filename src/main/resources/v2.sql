ALTER TABLE client
    ADD COLUMN phone_number varchar(20);

ALTER TABLE product
    ALTER COLUMN description DROP NOT NULL;

ALTER TABLE purchase
    ADD COLUMN purchase_date date;

ALTER TABLE purchase
    ADD COLUMN status varchar(50) DEFAULT 'Pending';

CREATE INDEX idx_product_sku
    ON product (sku);