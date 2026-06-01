package ma.atos.billing.invoice.billing_invoice.repository;

import ma.atos.billing.invoice.billing_invoice.entities.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, String> {
}
