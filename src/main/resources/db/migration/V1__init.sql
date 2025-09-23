-- Flyway baseline schema (V1)
-- Create initial tables according to JPA entities. This is a minimal baseline; extend as needed.

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    customer_id BIGINT,
    livreur_id BIGINT,
    active BOOLEAN
);

CREATE TABLE IF NOT EXISTS admin (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    pwd VARCHAR(255),
    surname VARCHAR(255),
    phone VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS livreur (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    surname VARCHAR(255),
    national_id VARCHAR(255),
    phone VARCHAR(255),
    email VARCHAR(255),
    vehicle_type VARCHAR(255),
    vehicle_brand VARCHAR(255),
    vehicle_color VARCHAR(255),
    vehicle_registration VARCHAR(255),
    photo_url VARCHAR(1024),
    comment VARCHAR(1024),
    active BOOLEAN
);

CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    surname VARCHAR(255),
    phone VARCHAR(255),
    code VARCHAR(255),
    whatsapp_phone VARCHAR(255),
    email VARCHAR(255),
    address1 VARCHAR(512),
    address2 VARCHAR(512),
    nui VARCHAR(255),
    rccm VARCHAR(255),
    company_name VARCHAR(255),
    segment VARCHAR(50),
    discount_percentage DOUBLE PRECISION,
    credit_balance DOUBLE PRECISION,
    last_activity_at TIMESTAMP,
    tags TEXT
);

CREATE TABLE IF NOT EXISTS addresses (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT REFERENCES customers(id) ON DELETE CASCADE,
    name VARCHAR(255),
    district VARCHAR(255),
    street VARCHAR(255),
    building_number VARCHAR(255),
    apartment_number VARCHAR(255),
    additional_instructions VARCHAR(1024),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    is_primary BOOLEAN
);

CREATE TABLE IF NOT EXISTS service_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(255) UNIQUE,
    name VARCHAR(255),
    mode VARCHAR(20),
    price DOUBLE PRECISION,
    description VARCHAR(1024),
    active BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    icon_url VARCHAR(1024),
    active BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS garments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    icon_url VARCHAR(1024),
    category_id BIGINT REFERENCES categories(id),
    active BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT REFERENCES customers(id),
    status VARCHAR(50),
    total_amount DOUBLE PRECISION,
    discount_amount DOUBLE PRECISION,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    item_type VARCHAR(255),
    quantity INTEGER,
    price_per_unit DOUBLE PRECISION,
    special_instructions VARCHAR(1024)
);

CREATE TABLE IF NOT EXISTS pickups (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    contact_name VARCHAR(255),
    contact_phone VARCHAR(255),
    address VARCHAR(1024),
    livreur_id BIGINT REFERENCES livreur(id),
    scheduled_date TIMESTAMP,
    actual_date TIMESTAMP,
    status VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS deliveries (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    contact_name VARCHAR(255),
    contact_phone VARCHAR(255),
    address VARCHAR(1024),
    livreur_id BIGINT REFERENCES livreur(id),
    scheduled_date TIMESTAMP,
    actual_date TIMESTAMP,
    status VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS invoices (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(255) UNIQUE NOT NULL,
    order_id BIGINT UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    customer_id BIGINT REFERENCES customers(id),
    issue_date TIMESTAMP,
    due_date DATE,
    status VARCHAR(50),
    total_amount DOUBLE PRECISION,
    paid_amount DOUBLE PRECISION,
    balance_amount DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT REFERENCES customers(id),
    invoice_id BIGINT REFERENCES invoices(id),
    payment_date TIMESTAMP,
    amount DOUBLE PRECISION,
    method VARCHAR(50),
    reference VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS expenses (
    id BIGSERIAL PRIMARY KEY,
    date DATE,
    amount DOUBLE PRECISION,
    category VARCHAR(50),
    reference VARCHAR(255),
    notes VARCHAR(1024),
    proof_url VARCHAR(1024),
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS claims (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT REFERENCES customers(id),
    order_id BIGINT REFERENCES orders(id),
    type VARCHAR(50),
    description VARCHAR(2048),
    status VARCHAR(50),
    resolution_notes VARCHAR(2048),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20),
    order_id BIGINT REFERENCES orders(id),
    customer_id BIGINT REFERENCES customers(id),
    title VARCHAR(255),
    description VARCHAR(1024),
    address_line VARCHAR(1024),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    scheduled_at TIMESTAMP,
    remind_before_minutes INTEGER,
    livreur_id BIGINT REFERENCES livreur(id),
    status VARCHAR(20),
    notes VARCHAR(1024),
    proof_photo_url VARCHAR(1024),
    proof_signature_url VARCHAR(1024),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS task_participants (
    task_id BIGINT REFERENCES tasks(id) ON DELETE CASCADE,
    phone VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS promotions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(255),
    description VARCHAR(1024),
    discount_percentage DOUBLE PRECISION,
    start_date DATE,
    end_date DATE,
    active BOOLEAN
);

CREATE TABLE IF NOT EXISTS livreur_locations (
    id BIGSERIAL PRIMARY KEY,
    livreur_id BIGINT REFERENCES livreur(id),
    order_id BIGINT REFERENCES orders(id),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    heading DOUBLE PRECISION,
    speed DOUBLE PRECISION,
    captured_at TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_livreur_loc_livreur_ts ON livreur_locations(livreur_id, captured_at);
CREATE INDEX IF NOT EXISTS idx_livreur_loc_order_ts ON livreur_locations(order_id, captured_at);
