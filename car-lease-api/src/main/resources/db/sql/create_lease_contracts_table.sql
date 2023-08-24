CREATE TABLE IF NOT EXISTS lease_contracts (
    contract_id  SERIAL PRIMARY KEY,
    customer_email VARCHAR(100),
    car_id VARCHAR(50),
    mileage DOUBLE PRECISION,
    start_date DATE,
    end_date DATE,
    duration INT,
    interest_rate DOUBLE PRECISION,
    nett_price DOUBLE PRECISION,
    monthly_lease_rate DOUBLE PRECISION,
    active BOOLEAN,
    quotation_id NUMERIC,
    contracted_by VARCHAR(100),
    contract_confirmation_time TIMESTAMP
);
