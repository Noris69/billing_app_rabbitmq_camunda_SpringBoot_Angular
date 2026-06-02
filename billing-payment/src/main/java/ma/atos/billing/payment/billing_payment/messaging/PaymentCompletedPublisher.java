package ma.atos.billing.payment.billing_payment.messaging;

import ma.atos.billing.payment.billing_payment.outbox.OutboxService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentCompletedPublisher {

    private final OutboxService outboxService;
    private final String exchange;
    private final String routingKey;

    public PaymentCompletedPublisher(
            OutboxService outboxService,
            @Value("${billing.rabbitmq.exchange}") String exchange,
            @Value("${billing.rabbitmq.payment-completed-routing-key}") String routingKey
    ) {
        this.outboxService = outboxService;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publish(PaymentCompletedEvent event) {
        outboxService.enqueue("Payment", event.paymentId(), event.eventType(), exchange, routingKey, event);
    }
}
