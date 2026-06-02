package ma.atos.billing.invoice.billing_invoice.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OutboxService {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;
    private final Tracer tracer;

    public OutboxService(OutboxEventRepository repository, ObjectMapper objectMapper, Tracer tracer) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.tracer = tracer;
    }

    public void enqueue(
            String aggregateType,
            Long aggregateId,
            String eventType,
            String exchangeName,
            String routingKey,
            Object payload
    ) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setExchangeName(exchangeName);
        event.setRoutingKey(routingKey);
        event.setPayload(toJson(payload));
        event.setCreatedAt(LocalDateTime.now());
        attachTrace(event);
        repository.save(event);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Impossible de serialiser l'evenement outbox", ex);
        }
    }

    private void attachTrace(OutboxEvent event) {
        Span span = tracer.currentSpan();
        if (span != null) {
            event.setTraceId(span.context().traceId());
            event.setSpanId(span.context().spanId());
        }
    }
}
