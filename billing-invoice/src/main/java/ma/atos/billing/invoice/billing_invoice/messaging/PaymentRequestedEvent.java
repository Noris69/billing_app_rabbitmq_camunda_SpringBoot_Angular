package ma.atos.billing.invoice.billing_invoice.messaging;

import ma.atos.billing.invoice.billing_invoice.enums.ModeReglement;

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
        Double amount,
        String currency,
        ModeReglement modeReglement,
        String description,
        Boolean paymentSuccess
) {
}
