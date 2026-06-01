package ma.atos.billing.invoice.billing_invoice.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentCompletedEvent(
        String eventId,
        String eventType,
        LocalDateTime occurredAt,
        Long paymentId,
        Long invoiceId,
        String invoiceReference,
        String transactionReference,
        BigDecimal amount,
        String currency,
        String status,
        String failureReason
) {
}
