INSERT INTO customer.customer (id, nom, prenom, adresse, payment_type, created_date, updated_date)
VALUES
    (9001, 'Hajoui', 'Hammid', 'Rabat', 'CARTE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9002, 'Moqnin', 'Karim', 'Rabat', 'ESPECES', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9003, 'Ascensio', 'Raul', 'Madrid, Maroc', 'CARTE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9004, 'Chrif', 'Farid', 'Nador, Maroc', 'ESPECES', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (9005, 'Angular', 'Test', 'Casablanca, Maroc', 'CARTE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET
    nom = EXCLUDED.nom,
    prenom = EXCLUDED.prenom,
    adresse = EXCLUDED.adresse,
    payment_type = EXCLUDED.payment_type,
    updated_date = CURRENT_TIMESTAMP;

