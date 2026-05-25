package ma.atos.billing.invoice.billing_invoice.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name = "type_point_de_vente",
        discriminatorType = DiscriminatorType.STRING
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "point_de_vente", schema = "invoice")
public class PointDeVente extends BusnessObject {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_seq")
    @SequenceGenerator(
            name = "global_seq",
            sequenceName = "global_sequence",
            allocationSize = 1
    )
    private Long id;

    private String nom;

    private String adresse;

    private String telephone;

    @OneToMany(mappedBy = "pointDeVente")
    private List<Invoice> invoices ;


}
