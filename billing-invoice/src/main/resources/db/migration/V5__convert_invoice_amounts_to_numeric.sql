alter table invoice.invoice
    alter column montant_ht type numeric(19, 2) using round(montant_ht::numeric, 2),
    alter column montant_tva type numeric(19, 2) using round(montant_tva::numeric, 2),
    alter column montant_ttc type numeric(19, 2) using round(montant_ttc::numeric, 2);
