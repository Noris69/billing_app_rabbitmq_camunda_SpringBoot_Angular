package ma.atos.billing.payment.billing_payment.repositories;

import ma.atos.billing.payment.billing_payment.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    List<Payment> findByCustomerId(Long customerId);

    boolean existsByCustomerId(Long customerId);

    List<Payment> findByCreancierId(Long creancierId);
}
