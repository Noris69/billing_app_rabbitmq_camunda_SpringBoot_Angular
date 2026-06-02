package ma.atos.billing.payment.billing_payment.services;

import ma.atos.billing.payment.billing_payment.dtos.PaymentDto;
import ma.atos.billing.payment.billing_payment.enums.PaymentStatus;
import ma.atos.billing.payment.billing_payment.mappers.PaymentMapper;
import ma.atos.billing.payment.billing_payment.messaging.PaymentCompletedEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class PaymentEventFactory {

    private final PaymentMapper mapper;

    public PaymentEventFactory(PaymentMapper mapper) {
        this.mapper = mapper;
    }

    public PaymentCompletedEvent completedEvent(
            PaymentDto payment,
            Long invoiceId,
            String invoiceReference,
            PaymentStatus status
    ) {
        return new PaymentCompletedEvent(
                UUID.randomUUID().toString(),
                "PaymentCompleted",
                LocalDateTime.now(),
                payment.id(),
                invoiceId,
                invoiceReference,
                "PAY-" + payment.id(),
                payment.amount(),
                payment.currency(),
                status.name(),
                mapper.failureReason(status)
        );
    }
}
