CREATE TABLE IF NOT EXISTS customer.customer (
    id BIGINT PRIMARY KEY,
    nom VARCHAR(255),
    prenom VARCHAR(255),
    adresse VARCHAR(255),
    payment_type VARCHAR(255),
    created_date TIMESTAMP,
    updated_date TIMESTAMP
);
