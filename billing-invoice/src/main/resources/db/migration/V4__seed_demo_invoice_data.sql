INSERT INTO invoice.customer (id, nom, prenom, cin, email, telephone, adresse, ville, created_date, updated_date)
VALUES
    (9001, 'Hajoui', 'Hammid', 'CIN9001', 'hammid.hajoui@example.com', '0611111111', 'Rabat', 'Rabat', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9002, 'Moqnin', 'Karim', 'CIN9002', 'karim.moqnin@example.com', '0622222222', 'Rabat', 'Rabat', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9003, 'Ascensio', 'Raul', 'CIN9003', 'raul.ascensio@example.com', '0633333333', 'Madrid, Maroc', 'Casablanca', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9004, 'Chrif', 'Farid', 'CIN9004', 'farid.chrif@example.com', '0644444444', 'Nador, Maroc', 'Nador', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9005, 'Angular', 'Test', 'CIN9005', 'angular.test@example.com', '0655555555', 'Casablanca, Maroc', 'Casablanca', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET
    nom = EXCLUDED.nom,
    prenom = EXCLUDED.prenom,
    cin = EXCLUDED.cin,
    email = EXCLUDED.email,
    telephone = EXCLUDED.telephone,
    adresse = EXCLUDED.adresse,
    ville = EXCLUDED.ville,
    updated_date = CURRENT_TIMESTAMP;

INSERT INTO invoice.creancier (id, nom, type_creancier, ice, rc, rib, banque, email, telephone, adresse, created_date, updated_date)
VALUES
    (9101, 'Maroc Telecom', 'IAM', 'ICE9101', 'RC9101', 'RIB9101', 'Attijariwafa Bank', 'facturation@iam.example.com', '0522000001', 'Rabat', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9102, 'ONEE', 'ONEE', 'ICE9102', 'RC9102', 'RIB9102', 'Banque Populaire', 'facturation@onee.example.com', '0522000002', 'Casablanca', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9103, 'Clinique Atlas', 'CLINIQUE', 'ICE9103', 'RC9103', 'RIB9103', 'CIH Bank', 'billing@clinique-atlas.example.com', '0522000003', 'Marrakech', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9104, 'CIH Bank', 'BANQUE', 'ICE9104', 'RC9104', 'RIB9104', 'CIH Bank', 'support@cih.example.com', '0522000004', 'Casablanca', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9105, 'Creancier Demo', 'AUTRE', 'ICE9105', 'RC9105', 'RIB9105', 'Bank Demo', 'demo@creancier.example.com', '0522000005', 'Tanger', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET
    nom = EXCLUDED.nom,
    type_creancier = EXCLUDED.type_creancier,
    ice = EXCLUDED.ice,
    rc = EXCLUDED.rc,
    rib = EXCLUDED.rib,
    banque = EXCLUDED.banque,
    email = EXCLUDED.email,
    telephone = EXCLUDED.telephone,
    adresse = EXCLUDED.adresse,
    updated_date = CURRENT_TIMESTAMP;

INSERT INTO invoice.point_de_vente (
    id, type_point_de_vente, nom, adresse, telephone,
    code_agence, responsable, region, type_agence,
    code_distributeur, zone_distribution, nom_commercial, commission,
    created_date, updated_date
)
VALUES
    (9201, 'AGENCE', 'Agence Rabat Centre', 'Rabat', '0537000001', 'AG-RBT-001', 'Amina Alaoui', 'Rabat-Sale-Kenitra', 'URBAINE', NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9202, 'AGENCE', 'Agence Casablanca Maarif', 'Casablanca', '0522000006', 'AG-CASA-002', 'Youssef Amrani', 'Casablanca-Settat', 'URBAINE', NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9203, 'DISTRIBUTEUR', 'Distributeur Nord', 'Tanger', '0539000003', NULL, NULL, NULL, NULL, 'DST-NORD-001', 'Nord', 'Nord Distribution', 2.5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9204, 'DISTRIBUTEUR', 'Distributeur Oriental', 'Oujda', '0536000004', NULL, NULL, NULL, NULL, 'DST-ORI-002', 'Oriental', 'Oriental Services', 3.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9205, 'AGENCE', 'Agence Demo', 'Fes', '0535000005', 'AG-FES-005', 'Sara Bennani', 'Fes-Meknes', 'REGIONALE', NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET
    type_point_de_vente = EXCLUDED.type_point_de_vente,
    nom = EXCLUDED.nom,
    adresse = EXCLUDED.adresse,
    telephone = EXCLUDED.telephone,
    code_agence = EXCLUDED.code_agence,
    responsable = EXCLUDED.responsable,
    region = EXCLUDED.region,
    type_agence = EXCLUDED.type_agence,
    code_distributeur = EXCLUDED.code_distributeur,
    zone_distribution = EXCLUDED.zone_distribution,
    nom_commercial = EXCLUDED.nom_commercial,
    commission = EXCLUDED.commission,
    updated_date = CURRENT_TIMESTAMP;

INSERT INTO invoice.invoice (
    id, reference, date_invoice, date_due, montant_ht, montant_tva, montant_ttc,
    status, mode_reglement, description, customer_id, creancier_id, point_de_vente_id,
    created_date, updated_date
)
VALUES
    (9301, 'INV-DEMO-20260529-001', CURRENT_DATE - 5, CURRENT_DATE + 25, 1000, 200, 1200, 'PAYEE', 'CARTE', 'Facture demo payee', 9001, 9101, 9201, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days'),
    (9302, 'INV-DEMO-20260529-002', CURRENT_DATE - 3, CURRENT_DATE + 27, 500, 100, 600, 'REJECTED', 'CARTE', 'Facture demo rejetee', 9002, 9102, 9202, CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days'),
    (9303, 'INV-DEMO-20260529-003', CURRENT_DATE, CURRENT_DATE + 30, 250, 50, 300, 'EN_ATTENTE', 'ESPECES', 'Facture demo en attente', 9003, 9103, 9203, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9304, 'INV-DEMO-20260529-004', CURRENT_DATE - 15, CURRENT_DATE - 1, 750, 150, 900, 'EN_RETARD', 'CARTE', 'Facture demo en retard', 9004, 9104, 9204, CURRENT_TIMESTAMP - INTERVAL '15 days', CURRENT_TIMESTAMP - INTERVAL '1 day')
ON CONFLICT (id) DO UPDATE SET
    reference = EXCLUDED.reference,
    date_invoice = EXCLUDED.date_invoice,
    date_due = EXCLUDED.date_due,
    montant_ht = EXCLUDED.montant_ht,
    montant_tva = EXCLUDED.montant_tva,
    montant_ttc = EXCLUDED.montant_ttc,
    status = EXCLUDED.status,
    mode_reglement = EXCLUDED.mode_reglement,
    description = EXCLUDED.description,
    customer_id = EXCLUDED.customer_id,
    creancier_id = EXCLUDED.creancier_id,
    point_de_vente_id = EXCLUDED.point_de_vente_id,
    updated_date = CURRENT_TIMESTAMP;

SELECT setval(
    'invoice.global_sequence',
    (
        SELECT GREATEST(
            COALESCE((SELECT MAX(id) FROM invoice.customer), 1),
            COALESCE((SELECT MAX(id) FROM invoice.creancier), 1),
            COALESCE((SELECT MAX(id) FROM invoice.point_de_vente), 1),
            COALESCE((SELECT MAX(id) FROM invoice.invoice), 1)
        )
    ),
    true
);
