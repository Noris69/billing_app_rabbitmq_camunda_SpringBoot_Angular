package ma.atos.billing.payment.billing_payment.messaging;

import java.time.LocalDateTime;

public record PaymentCompletedEvent(
        String eventId,
        String eventType,
        LocalDateTime occurredAt,
        Long paymentId,
        Long invoiceId,
        String invoiceReference,
        String transactionReference,
        Double amount,
        String currency,
        String status,
        String failureReason
) {
}
