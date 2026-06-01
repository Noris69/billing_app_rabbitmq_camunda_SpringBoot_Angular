alter table payment."transaction"
    alter column montant type numeric(19, 2) using round(montant::numeric, 2);
