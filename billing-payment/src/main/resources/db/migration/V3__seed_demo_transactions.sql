INSERT INTO payment."transaction" (
    id, created_date, updated_date, creancier_id, date, montant, operation_type, pv_id, customer_id, status
)
VALUES
    (9401, CURRENT_DATE - 5, CURRENT_DATE - 5, 9101, CURRENT_DATE - 5, 1200, 'CARTE', 9201, 9001, 'SUCCESS'),
    (9402, CURRENT_DATE - 3, CURRENT_DATE - 3, 9102, CURRENT_DATE - 3, 600, 'CARTE', 9202, 9002, 'FAILED'),
    (9403, CURRENT_DATE - 1, CURRENT_DATE - 1, 9104, CURRENT_DATE - 1, 900, 'CARTE', 9204, 9004, 'PENDING')
ON CONFLICT (id) DO UPDATE SET
    created_date = EXCLUDED.created_date,
    updated_date = EXCLUDED.updated_date,
    creancier_id = EXCLUDED.creancier_id,
    date = EXCLUDED.date,
    montant = EXCLUDED.montant,
    operation_type = EXCLUDED.operation_type,
    pv_id = EXCLUDED.pv_id,
    customer_id = EXCLUDED.customer_id,
    status = EXCLUDED.status;

SELECT setval(
    'payment.global_sequence',
    COALESCE((SELECT MAX(id) FROM payment."transaction"), 1),
    true
);
