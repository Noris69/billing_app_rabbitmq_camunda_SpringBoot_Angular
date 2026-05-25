package ma.atos.billing.invoice.billing_invoice.dtos;

/**
 * Matches discriminator values configured on entities: {@code @DiscriminatorValue("AGENCE")} and {@code "DISTRIBUTEUR"}.
 */
public enum PointDeVenteType {
    AGENCE,
    DISTRIBUTEUR
}
