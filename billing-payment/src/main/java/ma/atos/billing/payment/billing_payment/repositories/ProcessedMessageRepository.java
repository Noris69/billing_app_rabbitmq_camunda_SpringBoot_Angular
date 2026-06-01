package ma.atos.billing.payment.billing_payment.repositories;

import ma.atos.billing.payment.billing_payment.entities.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, String> {
}
