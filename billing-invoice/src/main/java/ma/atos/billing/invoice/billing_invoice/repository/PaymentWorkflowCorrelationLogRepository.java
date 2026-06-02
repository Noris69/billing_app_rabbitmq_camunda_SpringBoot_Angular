package ma.atos.billing.invoice.billing_invoice.repository;

import ma.atos.billing.invoice.billing_invoice.entities.PaymentWorkflowCorrelationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentWorkflowCorrelationLogRepository extends JpaRepository<PaymentWorkflowCorrelationLog, Long> {
}
