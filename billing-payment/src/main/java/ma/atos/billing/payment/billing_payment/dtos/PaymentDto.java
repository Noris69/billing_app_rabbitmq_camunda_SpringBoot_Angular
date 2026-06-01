package ma.atos.billing.payment.billing_payment.dtos;

import ma.atos.billing.payment.billing_payment.enums.ModeReglement;
import ma.atos.billing.payment.billing_payment.enums.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentDto(
        Long id,
        Long invoiceId,
        String invoiceReference,
        Long customerId,
        Long creancierId,
        Long pointDeVenteId,
        Double amount,
        String currency,
        ModeReglement modeReglement,
        String transactionReference,
        PaymentStatus status,
        String failureReason,
        Integer attemptNumber,
        Long parentPaymentId,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
