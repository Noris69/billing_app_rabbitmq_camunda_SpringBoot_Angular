CREATE SEQUENCE IF NOT EXISTS payment.global_sequence START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS payment."transaction" (
    id BIGINT PRIMARY KEY DEFAULT nextval('payment.global_sequence'),
    created_date DATE,
    updated_date DATE,
    creancier_id BIGINT,
    date DATE,
    montant DOUBLE PRECISION,
    operation_type VARCHAR(255),
    pv_id BIGINT,
    customer_id BIGINT
);
