CREATE TABLE IF NOT EXISTS customers (
    customer_id SERIAL PRIMARY KEY,
    name VARCHAR(50),
    street VARCHAR(50),
    house_number VARCHAR(50),
    zip_code VARCHAR(50),
    place VARCHAR(50),
    email_address VARCHAR(50),
    phone_number VARCHAR(50)
);
