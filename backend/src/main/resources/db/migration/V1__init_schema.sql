-- 客户表
CREATE TABLE customer (
    id          BIGSERIAL       PRIMARY KEY,
    username    VARCHAR(50)     NOT NULL UNIQUE,
    password    VARCHAR(255)    NOT NULL,
    email       VARCHAR(100),
    phone       VARCHAR(20),
    address     VARCHAR(255),
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  customer      IS '客户表';
COMMENT ON COLUMN customer.username IS '用户名';
COMMENT ON COLUMN customer.password IS '密码（BCrypt 加密）';
COMMENT ON COLUMN customer.email    IS '邮箱';
COMMENT ON COLUMN customer.phone    IS '手机号';
COMMENT ON COLUMN customer.address  IS '地址';

-- 商品表
CREATE TABLE product (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(200)    NOT NULL,
    description TEXT,
    price       DECIMAL(10,2)   NOT NULL CHECK (price > 0),
    stock       INT             NOT NULL DEFAULT 0 CHECK (stock >= 0),
    category    VARCHAR(50),
    author      VARCHAR(100),
    isbn        VARCHAR(20),
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  product          IS '商品表';
COMMENT ON COLUMN product.name        IS '商品名称';
COMMENT ON COLUMN product.description IS '商品描述';
COMMENT ON COLUMN product.price       IS '单价';
COMMENT ON COLUMN product.stock       IS '库存数量';
COMMENT ON COLUMN product.category    IS '商品分类';
COMMENT ON COLUMN product.author      IS '作者';
COMMENT ON COLUMN product.isbn        IS 'ISBN 编号';

-- 订单表
CREATE TABLE orders (
    id            BIGSERIAL       PRIMARY KEY,
    order_number  VARCHAR(50)     NOT NULL UNIQUE,
    customer_id   BIGINT          NOT NULL REFERENCES customer(id),
    total_amount  DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    status        VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    created_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  orders              IS '订单表';
COMMENT ON COLUMN orders.order_number IS '订单号';
COMMENT ON COLUMN orders.customer_id  IS '客户 ID';
COMMENT ON COLUMN orders.total_amount IS '订单总金额';
COMMENT ON COLUMN orders.status       IS '订单状态：PENDING/PAID/SHIPPED/CANCELLED';

-- 订单明细表
CREATE TABLE order_item (
    id            BIGSERIAL       PRIMARY KEY,
    order_id      BIGINT          NOT NULL REFERENCES orders(id),
    product_id    BIGINT          NOT NULL REFERENCES product(id),
    quantity      INT             NOT NULL CHECK (quantity > 0),
    unit_price    DECIMAL(10,2)   NOT NULL,
    subtotal      DECIMAL(12,2)   NOT NULL
);

COMMENT ON TABLE  order_item            IS '订单明细表';
COMMENT ON COLUMN order_item.order_id   IS '订单 ID';
COMMENT ON COLUMN order_item.product_id IS '商品 ID';
COMMENT ON COLUMN order_item.quantity   IS '购买数量';
COMMENT ON COLUMN order_item.unit_price IS '下单时单价快照';
COMMENT ON COLUMN order_item.subtotal   IS '小计金额 = unit_price * quantity';

-- 索引
CREATE INDEX idx_orders_customer_id  ON orders(customer_id);
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_order_item_order_id ON order_item(order_id);
