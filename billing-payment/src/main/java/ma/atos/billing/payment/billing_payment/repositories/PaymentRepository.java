package ma.atos.billing.payment.billing_payment.repositories;

import ma.atos.billing.payment.billing_payment.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    List<Payment> findByCustomerId(Long customerId);

    boolean existsByCustomerId(Long customerId);

    List<Payment> findByCreancierId(Long creancierId);

    List<Payment> findByInvoiceIdOrderByAttemptNumberAscIdAsc(Long invoiceId);

    List<Payment> findByParentPaymentIdOrderByAttemptNumberAscIdAsc(Long parentPaymentId);

    long countByInvoiceId(Long invoiceId);

    long countByStatus(String status);

    long countByOperationType(String operationType);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.status = 'SUCCESS'")
    BigDecimal sumSuccessfulAmount();
}
