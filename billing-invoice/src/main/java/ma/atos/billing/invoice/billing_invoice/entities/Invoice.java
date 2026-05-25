package ma.atos.billing.invoice.billing_invoice.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.atos.billing.invoice.billing_invoice.enums.ModeReglement;
import ma.atos.billing.invoice.billing_invoice.enums.StatusInvoice;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invoice", schema = "invoice")
public class Invoice extends BusnessObject {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_seq")
    @SequenceGenerator(
            name = "global_seq",
            sequenceName = "global_sequence",
            allocationSize = 1
    )
    private Long id;

    @NotBlank
    private String reference;

    private LocalDate dateInvoice;

    private LocalDate dateDue;

    private Double montantHt;

    private Double montantTva;

    private Double montantTtc;

    @Enumerated(EnumType.STRING)
    private StatusInvoice status;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_reglement")
    private ModeReglement modeReglement;

    private String description;


    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "creancier_id")
    private  Creancier creancier  ;

    @ManyToOne
    @JoinColumn(name = "point_de_vente_id")
    private  PointDeVente pointDeVente;

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Creancier getCreancier() {
        return creancier;
    }

    public void setCreancier(Creancier creancier) {
        this.creancier = creancier;
    }

    public PointDeVente getPointDeVente() {
        return pointDeVente;
    }

    public void setPointDeVente(PointDeVente pointDeVente) {
        this.pointDeVente = pointDeVente;
    }
}
