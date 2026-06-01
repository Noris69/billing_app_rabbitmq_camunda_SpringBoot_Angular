ALTER TABLE IF EXISTS payment."transaction"
    ADD COLUMN IF NOT EXISTS invoice_id BIGINT,
    ADD COLUMN IF NOT EXISTS invoice_reference VARCHAR(255),
    ADD COLUMN IF NOT EXISTS attempt_number INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS parent_payment_id BIGINT,
    ADD COLUMN IF NOT EXISTS failure_reason VARCHAR(500);

UPDATE payment."transaction"
SET attempt_number = 1
WHERE attempt_number IS NULL OR attempt_number < 1;

UPDATE payment."transaction"
SET invoice_id = 9301,
    invoice_reference = 'INV-DEMO-20260529-001',
    failure_reason = NULL
WHERE id = 9401 AND invoice_id IS NULL;

UPDATE payment."transaction"
SET invoice_id = 9302,
    invoice_reference = 'INV-DEMO-20260529-002',
    failure_reason = 'Paiement refuse par la simulation'
WHERE id = 9402 AND invoice_id IS NULL;

UPDATE payment."transaction"
SET invoice_id = 9304,
    invoice_reference = 'INV-DEMO-20260529-004',
    failure_reason = NULL
WHERE id = 9403 AND invoice_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_payment_transaction_invoice_id
    ON payment."transaction" (invoice_id);

CREATE INDEX IF NOT EXISTS idx_payment_transaction_parent_payment_id
    ON payment."transaction" (parent_payment_id);
