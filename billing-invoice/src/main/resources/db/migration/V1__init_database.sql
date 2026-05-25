
CREATE TABLE IF NOT EXISTS invoice.costumer (
    id BIGINT PRIMARY KEY DEFAULT nextval('global_sequence'),
    nom VARCHAR(255),
    prenom VARCHAR(255),
    cin VARCHAR(255),
    email VARCHAR(255),
    telephone VARCHAR(255),
    adresse VARCHAR(255),
    ville VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS invoice.creancier (
    id BIGINT PRIMARY KEY DEFAULT nextval('global_sequence'),
    nom VARCHAR(255),
    ice VARCHAR(255),
    rc VARCHAR(255),
    rib VARCHAR(255),
    banque VARCHAR(255),
    email VARCHAR(255),
    telephone VARCHAR(255),
    adresse VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS invoice.point_de_vente (
    id BIGINT PRIMARY KEY DEFAULT nextval('global_sequence'),
    type_point_de_vente VARCHAR(31) NOT NULL,
    nom VARCHAR(255),
    adresse VARCHAR(255),
    telephone VARCHAR(255),

    code_agence VARCHAR(255),
    responsable VARCHAR(255),
    region VARCHAR(255),
    type_agence VARCHAR(255),

    code_distributeur VARCHAR(255),
    zone_distribution VARCHAR(255),
    nom_commercial VARCHAR(255),
    commission DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS invoice.invoice (
    id BIGINT PRIMARY KEY DEFAULT nextval('global_sequence'),
    reference VARCHAR(255),
    date_invoice DATE,
    date_due DATE,
    montant_ht DOUBLE PRECISION,
    montant_tva DOUBLE PRECISION,
    montant_ttc DOUBLE PRECISION,
    status VARCHAR(255),
    description TEXT,

    costumer_id BIGINT,
    creancier_id BIGINT,
    point_de_vente_id BIGINT,

    CONSTRAINT fk_invoice_costumer
        FOREIGN KEY (costumer_id)
        REFERENCES invoice.costumer(id),

    CONSTRAINT fk_invoice_creancier
        FOREIGN KEY (creancier_id)
        REFERENCES invoice.creancier(id),

    CONSTRAINT fk_invoice_point_de_vente
        FOREIGN KEY (point_de_vente_id)
        REFERENCES invoice.point_de_vente(id)
);