CREATE TABLE IF NOT EXISTS cars (
    car_id SERIAL PRIMARY KEY,
    make VARCHAR(50),
    model VARCHAR(50),
    version VARCHAR(50),
    car_number_plate VARCHAR(20),
    number_of_doors INT,
    co2_emission INT,
    gross_price DOUBLE PRECISION,
    nett_price DOUBLE PRECISION
);
