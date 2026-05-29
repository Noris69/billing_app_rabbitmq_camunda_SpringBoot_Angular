DO $$
BEGIN
    IF to_regclass('invoice.costumer') IS NOT NULL
       AND to_regclass('invoice.customer') IS NULL THEN
        ALTER TABLE invoice.costumer RENAME TO customer;
    END IF;
END $$;
