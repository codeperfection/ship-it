CREATE TABLE auth_user
(
    uuid UUID,

    username TEXT NOT NULL,
    password TEXT NOT NULL,
    password_change_date TIMESTAMP WITH TIME ZONE NOT NULL,
    name TEXT NOT NULL,
    email TEXT NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    PRIMARY KEY (uuid),
    UNIQUE (username),
    UNIQUE (email)
);

CREATE TABLE role
(
    uuid UUID,

    name TEXT NOT NULL,

    PRIMARY KEY (uuid),
    UNIQUE (name)
);

INSERT INTO role VALUES ('6ff18911-8db8-4803-b8e8-7b36b0540753', 'ROLE_USER');

CREATE TABLE user_role
(
    user_uuid UUID,
    role_uuid UUID,

    PRIMARY KEY (user_uuid, role_uuid),
    FOREIGN KEY (user_uuid) REFERENCES auth_user (uuid),
    FOREIGN KEY (role_uuid) REFERENCES role (uuid)
);

CREATE INDEX idx_user_role_user_uuid ON user_role (user_uuid);

CREATE TABLE product
(
    uuid UUID,

    name text NOT NULL,
    volume INTEGER NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL,

    user_uuid UUID NOT NULL,

    PRIMARY KEY (uuid),
    FOREIGN KEY (user_uuid) REFERENCES auth_user (uuid)
);

CREATE INDEX product_user_uuid ON product (user_uuid);
CREATE INDEX product_is_active ON product (is_active);
CREATE INDEX product_created_at ON product (created_at);

CREATE TABLE stock_item
(
  uuid UUID,

  user_uuid UUID NOT NULL,
  product_uuid UUID NOT NULL,
  count INTEGER NOT NULL,

  PRIMARY KEY (uuid),
  FOREIGN KEY (user_uuid) REFERENCES auth_user (uuid),
  FOREIGN KEY (product_uuid) REFERENCES product (uuid),
  UNIQUE (user_uuid, product_uuid)
);

CREATE INDEX stock_item_user_uuid ON stock_item (user_uuid);

CREATE TABLE transporter
(
    uuid UUID,

    name text NOT NULL,
    capacity INTEGER NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL,

    user_uuid UUID NOT NULL,

    PRIMARY KEY (uuid),
    FOREIGN KEY (user_uuid) REFERENCES auth_user (uuid)
);

CREATE INDEX transporter_user_uuid ON transporter (user_uuid);
CREATE INDEX transporter_is_active ON transporter (is_active);
CREATE INDEX transporter_created_at ON transporter (created_at);

CREATE TABLE shipping
(
    uuid UUID,

    name text NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    time_zone_name TEXT NOT NULL,

    user_uuid UUID NOT NULL,
    transporter_uuid UUID NOT NULL,

    PRIMARY KEY (uuid),
    FOREIGN KEY (user_uuid) REFERENCES auth_user (uuid),
    FOREIGN KEY (transporter_uuid) REFERENCES transporter (uuid)
);

CREATE INDEX shipping_user_uuid ON shipping (user_uuid);
CREATE INDEX shipping_created_at ON shipping (created_at);

CREATE TABLE shipped_item
(
    uuid UUID,

    count INTEGER NOT NULL,

    shipping_uuid UUID NOT NULL,
    product_uuid UUID NOT NULL,

    PRIMARY KEY (uuid),
    FOREIGN KEY (shipping_uuid) REFERENCES shipping (uuid),
    FOREIGN KEY (product_uuid) REFERENCES product (uuid),
    UNIQUE (shipping_uuid, product_uuid)
);

CREATE INDEX shipped_item_shipping_uuid ON shipped_item (shipping_uuid);
