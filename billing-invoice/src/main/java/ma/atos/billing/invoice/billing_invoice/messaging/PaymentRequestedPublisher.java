package ma.atos.billing.invoice.billing_invoice.messaging;

import ma.atos.billing.invoice.billing_invoice.entities.Invoice;
import ma.atos.billing.invoice.billing_invoice.outbox.OutboxService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class PaymentRequestedPublisher {

    private final OutboxService outboxService;
    private final String exchange;
    private final String routingKey;

    public PaymentRequestedPublisher(
            OutboxService outboxService,
            @Value("${billing.rabbitmq.exchange}") String exchange,
            @Value("${billing.rabbitmq.payment-requested-routing-key}") String routingKey
    ) {
        this.outboxService = outboxService;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publish(Invoice invoice) {
        publish(
                invoice,
                invoice.getCustomer() != null ? invoice.getCustomer().getId() : null,
                invoice.getCreancier() != null ? invoice.getCreancier().getId() : null,
                invoice.getPointDeVente() != null ? invoice.getPointDeVente().getId() : null,
                null
        );
    }

    public void publish(Invoice invoice, Long customerId, Long creancierId, Long pointDeVenteId, Boolean paymentSuccess) {
        PaymentRequestedEvent event = new PaymentRequestedEvent(
                UUID.randomUUID().toString(),
                "PaymentRequested",
                LocalDateTime.now(),
                invoice.getId(),
                invoice.getReference(),
                customerId,
                creancierId,
                pointDeVenteId,
                invoice.getMontantTtc(),
                "MAD",
                invoice.getModeReglement(),
                invoice.getDescription(),
                paymentSuccess
        );

        outboxService.enqueue("Invoice", invoice.getId(), event.eventType(), exchange, routingKey, event);
    }
}
