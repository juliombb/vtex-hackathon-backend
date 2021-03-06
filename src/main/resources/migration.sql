 CREATE TABLE customer(
   id BIGSERIAL PRIMARY KEY,
   name VARCHAR(255) NOT NULL,
   cpf VARCHAR(30) NOT NULL,
   email VARCHAR(255) NOT NULL,
   phone VARCHAR(15),
   gender VARCHAR(6),
   birth_date TIMESTAMP
);

CREATE TABLE market(
    id BIGSERIAL PRIMARY KEY,
    address VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    asset TEXT
);

CREATE TABLE product(
    id VARCHAR(255) PRIMARY KEY,
    category VARCHAR(30) NOT NULL,
    asset TEXT,
    description TEXT,
    price BIGINT NOT NULL 
);

CREATE TABLE shelf(
    id BIGSERIAL PRIMARY KEY,
    category VARCHAR(30) NOT NULL
);

CREATE TABLE cash_box(
   id BIGSERIAL PRIMARY KEY,
   status VARCHAR(30) NOT NULL,
   market_id BIGINT NOT NULL
);

CREATE TABLE category (
    id VARCHAR(30) PRIMARY KEY,
    description TEXT
);

CREATE TABLE purchase (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    cash_box_id BIGINT NOT NULL,
    status VARCHAR(255) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    total BIGINT NOT NULL
);

CREATE TABLE purchase_item(
  purchase_id BIGINT NOT NULL,
  product_id VARCHAR(255) NOT NULL,
  quantity INT NOT NULL,
  PRIMARY KEY (purchase_id, product_id)
);

CREATE TABLE wish_list(
  wish_list_id BIGSERIAL PRIMARY KEY,
  customer_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL,
  active BOOLEAN DEFAULT true
);

CREATE TABLE wish_list_item(
  wish_list_id BIGINT NOT NULL,
  product_id VARCHAR(255) NOT NULL,
  quantity INT NOT NULL,
  PRIMARY KEY (wish_list_id, product_id)
);