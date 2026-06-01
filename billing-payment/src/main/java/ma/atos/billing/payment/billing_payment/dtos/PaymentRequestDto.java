package ma.atos.billing.payment.billing_payment.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ma.atos.billing.payment.billing_payment.enums.ModeReglement;
import ma.atos.billing.payment.billing_payment.enums.PaymentStatus;

public record PaymentRequestDto(
        @NotNull Long invoiceId,
        String invoiceReference,
        Long customerId,
        Long creancierId,
        Long pointDeVenteId,
        @NotNull @Positive Double amount,
        String currency,
        ModeReglement modeReglement,
        String description,
        Boolean paymentSuccess,
        PaymentStatus status
) {
}
