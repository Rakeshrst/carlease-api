CREATE TABLE IF NOT EXISTS lease_quotations (
    quotation_id SERIAL PRIMARY KEY,
    customer_email VARCHAR(100),
    car_id VARCHAR(50),
    mileage DOUBLE PRECISION,
    expected_start_date DATE,
    duration INT,
    interest_rate DOUBLE PRECISION,
    nett_price DOUBLE PRECISION,
    monthly_lease_rate DOUBLE PRECISION,
    contract BOOLEAN,
    quotation_by VARCHAR(100),
    contracted_by VARCHAR(100),
    quotation_create_time TIMESTAMP
);
