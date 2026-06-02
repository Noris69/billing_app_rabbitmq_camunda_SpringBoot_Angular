package ma.atos.billing.payment.billing_payment.mappers;

import ma.atos.billing.payment.billing_payment.dtos.PaymentDto;
import ma.atos.billing.payment.billing_payment.entities.Payment;
import ma.atos.billing.payment.billing_payment.enums.ModeReglement;
import ma.atos.billing.payment.billing_payment.enums.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class PaymentMapper {

    public PaymentDto toDto(Payment payment) {
        PaymentStatus status = toPaymentStatus(payment.getStatus());
        return new PaymentDto(
                payment.getId(),
                payment.getInvoiceId(),
                payment.getInvoiceReference(),
                payment.getCustomerId(),
                payment.getCreancierId(),
                payment.getPointDeVenteId(),
                payment.getAmount(),
                "MAD",
                toModeReglement(payment.getOperationType()),
                "PAY-" + payment.getId(),
                status,
                payment.getFailureReason() != null ? payment.getFailureReason() : failureReason(status),
                payment.getAttemptNumber(),
                payment.getParentPaymentId(),
                toDateTime(payment.getCreatedDate(), payment.getDate()),
                toDateTime(payment.getUpdatedDate(), payment.getDate())
        );
    }

    public ModeReglement toModeReglement(String value) {
        if (!hasText(value)) {
            return null;
        }

        try {
            return ModeReglement.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public PaymentStatus toPaymentStatus(String value) {
        if (!hasText(value)) {
            return PaymentStatus.PENDING;
        }

        try {
            return PaymentStatus.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Statut paiement invalide : " + value);
        }
    }

    public String failureReason(PaymentStatus status) {
        return status == PaymentStatus.FAILED || status == PaymentStatus.CANCELLED
                ? "Paiement refuse par la simulation"
                : null;
    }

    private LocalDateTime toDateTime(LocalDate primaryDate, LocalDate fallbackDate) {
        LocalDate date = primaryDate != null ? primaryDate : fallbackDate;
        return date != null ? date.atStartOfDay() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
