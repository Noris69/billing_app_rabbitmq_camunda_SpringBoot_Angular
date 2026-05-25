package ma.atos.billing.invoice.billing_invoice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.atos.billing.invoice.billing_invoice.entities.BusnessObject;
import ma.atos.billing.invoice.billing_invoice.entities.Invoice;
import ma.atos.billing.invoice.billing_invoice.enums.TypeCreancier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "creancier", schema = "invoice")
public class Creancier extends BusnessObject {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_seq")
    @SequenceGenerator(
            name = "global_seq",
            sequenceName = "global_sequence",
            allocationSize = 1
    )
    private Long id;

    @NotBlank
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_creancier")
    @NotNull
    private TypeCreancier typeCreancier;

    private String ice;

    private String rc;

    private String rib;

    private String banque;

    private String email;

    private String telephone;

    private String adresse;

    @OneToMany (mappedBy = "creancier")
    private List<Invoice> invoices ;

}