package ma.atos.billing.invoice.billing_invoice.dtos;

import lombok.Data;

@Data
public class PointDeVenteSearchCriteria {

    private String type_point_de_vente;

    private String nom;

    private String adresse;

    private String telephone;

    private String codeAgence;

    private String responsable;

    private String region;

    private String typeAgence;

    private String codeDistributeur;

    private String zoneDistribution;

    private String nomCommercial;

    private Double commission;

}
