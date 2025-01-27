
CREATE TABLE client (
    id int  NOT NULL,
    full_name varchar(255)  NOT NULL,
    email varchar(255)  NOT NULL,
    CONSTRAINT client_pk PRIMARY KEY (id)
);

CREATE TABLE product (
    id int  NOT NULL,
    product_category_id int  NOT NULL,
    sku char(10)  NOT NULL,
    name varchar(255)  NOT NULL,
    price decimal(12,2)  NOT NULL,
    description varchar(1000)  NOT NULL,
    image bytea  NOT NULL,
    CONSTRAINT product_pk PRIMARY KEY (id)
);

CREATE TABLE product_category (
    id int  NOT NULL,
    name varchar(255)  NOT NULL,
    parent_category_id int  NULL,
    CONSTRAINT product_category_pk PRIMARY KEY (id)
);

CREATE TABLE purchase (
    id int  NOT NULL,
    purchase_no char(12)  NOT NULL,
    client_id int  NOT NULL,
    shipment_id int  NOT NULL,
    CONSTRAINT purchase_pk PRIMARY KEY (id)
);

CREATE TABLE purchase_item (
    id int  NOT NULL,
    purchase_id int  NOT NULL,
    amount int  NOT NULL,
    product_id int  NOT NULL,
    CONSTRAINT purchase_item_pk PRIMARY KEY (id)
);

CREATE TABLE shipment (
    id int  NOT NULL,
    date date  NOT NULL,
    cost int  NOT NULL,
    CONSTRAINT shipment_pk PRIMARY KEY (id)
);

ALTER TABLE purchase ADD CONSTRAINT client_purchase
    FOREIGN KEY (client_id)
    REFERENCES client (id)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

ALTER TABLE product ADD CONSTRAINT product_category_product
    FOREIGN KEY (product_category_id)
    REFERENCES product_category (id)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

ALTER TABLE product_category ADD CONSTRAINT product_category_product_category
    FOREIGN KEY (parent_category_id)
    REFERENCES product_category (id)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

ALTER TABLE purchase_item ADD CONSTRAINT purchase_item_product
    FOREIGN KEY (product_id)
    REFERENCES product (id)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

ALTER TABLE purchase_item ADD CONSTRAINT purchase_purchase_item
    FOREIGN KEY (purchase_id)
    REFERENCES purchase (id)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

ALTER TABLE purchase ADD CONSTRAINT purchase_shipment
    FOREIGN KEY (shipment_id)
    REFERENCES shipment (id)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;


