CREATE TABLE product
(
    id UUID,
    user_id UUID NOT NULL,

    name VARCHAR(256) NOT NULL,
    volume INTEGER NOT NULL,
    price INTEGER NOT NULL,
    count_in_stock INTEGER NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL,

    PRIMARY KEY (id)
);

CREATE INDEX product_user_active_date ON product (user_id, is_active, created_at);

CREATE TABLE transporter
(
    id UUID,
    user_id UUID NOT NULL,

    name VARCHAR(256) NOT NULL,
    capacity INTEGER NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL,

    PRIMARY KEY (id)
);

CREATE INDEX transporter_user_active_date ON transporter (user_id, is_active, created_at);

CREATE TABLE shipping
(
    id UUID,
    user_id UUID NOT NULL,

    name VARCHAR(256) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    transporter_id UUID NOT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (transporter_id) REFERENCES transporter (id)
);

CREATE INDEX shipping_user_date ON shipping (user_id, created_at);

CREATE TABLE shipped_item
(
    id UUID,

    quantity INTEGER NOT NULL,

    shipping_id UUID NOT NULL,
    product_id UUID NOT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (shipping_id) REFERENCES shipping (id),
    FOREIGN KEY (product_id) REFERENCES product (id),
    UNIQUE (shipping_id, product_id)
);

CREATE INDEX shipped_item_shipping_id ON shipped_item (shipping_id);
