package ma.atos.billing.invoice.billing_invoice.enums;

// Type de créancier (source de la facture)
public enum TypeCreancier {
    IAM,       // Maroc Telecom
    BANQUE,    // Attijariwafa, CIH, BMCE...
    ONEE,      // Electricité et eau
    CLINIQUE,  // Hôpitaux, cliniques privées
    AUTRE      // Tout autre type
}
