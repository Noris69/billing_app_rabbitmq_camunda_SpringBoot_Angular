package ma.atos.billing.payment.billing_payment.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentRequestedEvent(
        String eventId,
        String eventType,
        LocalDateTime occurredAt,
        Long invoiceId,
        String invoiceReference,
        Long customerId,
        Long creancierId,
        Long pointDeVenteId,
        BigDecimal amount,
        String currency,
        String modeReglement,
        String description,
        Boolean paymentSuccess
) {
}
