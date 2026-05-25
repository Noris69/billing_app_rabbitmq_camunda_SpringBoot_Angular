package ma.atos.billing.payment.billing_payment.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "\"transaction\"", schema = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_global_seq")
    @SequenceGenerator(
            name = "payment_global_seq",
            sequenceName = "payment.global_sequence",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    @Column(name = "creancier_id")
    private Long creancierId;

    private LocalDate date;

    @Column(name = "montant")
    private Double amount;

    @Column(name = "operation_type")
    private String operationType;

    @Column(name = "pv_id")
    private Long pointDeVenteId;

    @Column(name = "customer_id")
    private Long customerId;

    @PrePersist
    void prePersist() {
        LocalDate now = LocalDate.now();
        createdDate = now;
        updatedDate = now;
        if (date == null) {
            date = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedDate = LocalDate.now();
    }
}
