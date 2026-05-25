package ma.atos.billing.invoice.billing_invoice.entities;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("DISTRIBUTEUR")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Distributeur extends PointDeVente {

    private String codeDistributeur;

    private String zoneDistribution;

    private String nomCommercial;

    private Double commission;
}
