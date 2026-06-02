package ma.atos.billing.payment.billing_payment.outbox;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Component
public class OutboxMessageRelay {

    private static final int MAX_ATTEMPTS = 5;

    private final OutboxEventRepository repository;
    private final RabbitTemplate rabbitTemplate;

    public OutboxMessageRelay(OutboxEventRepository repository, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelayString = "${billing.outbox.relay-delay-ms:2000}")
    @Transactional
    public void publishPendingEvents() {
        repository.findTop20ByPublishedAtIsNullAndAttemptsLessThanOrderByCreatedAtAsc(MAX_ATTEMPTS)
                .forEach(this::publish);
    }

    private void publish(OutboxEvent event) {
        try {
            rabbitTemplate.send(event.getExchangeName(), event.getRoutingKey(), toMessage(event));
            event.setPublishedAt(LocalDateTime.now());
            event.setLastError(null);
        } catch (RuntimeException ex) {
            event.setAttempts(event.getAttempts() + 1);
            event.setLastError(ex.getMessage());
        }
    }

    private Message toMessage(OutboxEvent event) {
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setMessageId(String.valueOf(event.getId()));
        properties.setHeader("eventType", event.getEventType());
        properties.setHeader("traceId", event.getTraceId());
        properties.setHeader("spanId", event.getSpanId());
        if (event.getTraceId() != null && event.getSpanId() != null) {
            properties.setHeader("b3", event.getTraceId() + "-" + event.getSpanId() + "-1");
        }
        return new Message(event.getPayload().getBytes(StandardCharsets.UTF_8), properties);
    }
}
