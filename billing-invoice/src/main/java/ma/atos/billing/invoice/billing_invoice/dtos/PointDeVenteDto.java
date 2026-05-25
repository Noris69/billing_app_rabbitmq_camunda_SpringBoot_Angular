package ma.atos.billing.invoice.billing_invoice.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointDeVenteDto {

    private Long id;

    /**
     * Type métier du point de vente (agence / distributeur).
     */
    @NotNull
    private PointDeVenteType type;

    @NotBlank
    private String nom;

    private String adresse;

    private String telephone;

    // --- Champs Agence ---
    private String codeAgence;

    private String responsable;

    private String region;

    private String typeAgence;

    // --- Champs Distributeur ---
    private String codeDistributeur;

    private String zoneDistribution;

    private String nomCommercial;

    @PositiveOrZero
    private Double commission;

    private Date createdDate;

    private Date updatedDate;
}