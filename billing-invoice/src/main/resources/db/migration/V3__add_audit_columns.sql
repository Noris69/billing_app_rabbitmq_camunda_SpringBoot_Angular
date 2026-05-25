-- V3 (consolidated): all changes after V2
-- Assumption: V2 has already been applied (invoice.costumer table renamed to invoice.customer)

-- 1) Auditing columns used by BusnessObject (Spring Data JPA auditing)
ALTER TABLE invoice.customer
    ADD COLUMN IF NOT EXISTS created_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_date TIMESTAMP;

ALTER TABLE invoice.creancier
    ADD COLUMN IF NOT EXISTS created_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_date TIMESTAMP;

ALTER TABLE invoice.point_de_vente
    ADD COLUMN IF NOT EXISTS created_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_date TIMESTAMP;

ALTER TABLE invoice.invoice
    ADD COLUMN IF NOT EXISTS created_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_date TIMESTAMP;

-- 2) Add creditor type (IAM/BANQUE/ONEE/...) to support different business rules per source
ALTER TABLE invoice.creancier
    ADD COLUMN IF NOT EXISTS type_creancier VARCHAR(50);

-- 3) Rename FK column costumer_id -> customer_id (typo fix)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'invoice'
          AND table_name = 'invoice'
          AND column_name = 'costumer_id'
    ) THEN
        ALTER TABLE invoice.invoice DROP CONSTRAINT IF EXISTS fk_invoice_costumer;
        ALTER TABLE invoice.invoice DROP CONSTRAINT IF EXISTS fk_invoice_customer;

        ALTER TABLE invoice.invoice RENAME COLUMN costumer_id TO customer_id;

        ALTER TABLE invoice.invoice
            ADD CONSTRAINT fk_invoice_customer
                FOREIGN KEY (customer_id)
                REFERENCES invoice.customer(id);
    END IF;
END $$;

-- 4) Add payment mode on invoices
ALTER TABLE invoice.invoice
    ADD COLUMN IF NOT EXISTS mode_reglement VARCHAR(50);
