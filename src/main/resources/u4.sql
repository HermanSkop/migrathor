ALTER TABLE shipment
    ALTER COLUMN "cost" SET DATA TYPE INTEGER
        USING "cost"::numeric::integer;

ALTER TABLE product
    ALTER COLUMN "price" SET DATA TYPE INTEGER
        USING "price"::numeric::integer;
