package ma.atos.billing.invoice.billing_invoice.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.atos.billing.invoice.billing_invoice.entities.BusnessObject;
import ma.atos.billing.invoice.billing_invoice.entities.Invoice;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer", schema = "invoice")
public class Customer extends BusnessObject {

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

    @NotBlank
    private String prenom;

    @NotBlank
    private String cin;

    @Email
    private String email;

    private String telephone;

    private String adresse;

    private String ville;

    @OneToMany(mappedBy = "customer")
    private List<Invoice> invoices ;

}