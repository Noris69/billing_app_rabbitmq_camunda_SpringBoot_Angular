package ma.atos.billing.payment.billing_payment.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentCompletedPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public PaymentCompletedPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${billing.rabbitmq.exchange}") String exchange,
            @Value("${billing.rabbitmq.payment-completed-routing-key}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publish(PaymentCompletedEvent event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
