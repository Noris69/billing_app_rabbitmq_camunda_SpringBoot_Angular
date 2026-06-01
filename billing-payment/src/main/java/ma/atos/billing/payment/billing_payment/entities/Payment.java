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

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "invoice_reference")
    private String invoiceReference;

    private LocalDate date;

    @Column(name = "montant")
    private Double amount;

    @Column(name = "operation_type")
    private String operationType;

    @Column(name = "status")
    private String status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "attempt_number")
    private Integer attemptNumber;

    @Column(name = "parent_payment_id")
    private Long parentPaymentId;

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
        if (attemptNumber == null || attemptNumber < 1) {
            attemptNumber = 1;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedDate = LocalDate.now();
    }
}
