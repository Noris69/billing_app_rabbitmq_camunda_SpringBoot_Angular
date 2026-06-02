package ma.atos.billing.invoice.billing_invoice.messaging;

import ma.atos.billing.invoice.billing_invoice.entities.ProcessedMessage;
import ma.atos.billing.invoice.billing_invoice.repository.ProcessedMessageRepository;
import ma.atos.billing.invoice.billing_invoice.services.InvoiceService;
import ma.atos.billing.invoice.billing_invoice.workflow.InvoiceWorkflowVariables;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class PaymentCompletedListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentCompletedListener.class);

    private final InvoiceService invoiceService;
    private final ProcessedMessageRepository processedMessageRepository;
    private final RuntimeService runtimeService;

    public PaymentCompletedListener(
            InvoiceService invoiceService,
            ProcessedMessageRepository processedMessageRepository,
            RuntimeService runtimeService
    ) {
        this.invoiceService = invoiceService;
        this.processedMessageRepository = processedMessageRepository;
        this.runtimeService = runtimeService;
    }

    @Transactional
    @RabbitListener(queues = "${billing.rabbitmq.payment-completed-queue}")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        if (isDuplicate(event.eventId())) {
            LOGGER.info("Message paiement complete deja traite ignore. eventId={}", event.eventId());
            return;
        }
        markAsProcessed(event.eventId(), event.eventType());

        // 1. Tenter d'abord de corréler le workflow Camunda
        boolean correlated = correlateWorkflow(event);

        // 2. Si aucune instance n'a été corrélée, on applique la mise à jour en direct comme fallback
        if (!correlated) {
            LOGGER.info("Aucune instance Camunda corrélée. Mise à jour directe de la facture. invoiceId={}", event.invoiceId());
            if ("SUCCESS".equalsIgnoreCase(event.status())) {
                invoiceService.markPaid(event.invoiceId());
            } else {
                invoiceService.markRejected(event.invoiceId());
            }
        }
    }

    private boolean correlateWorkflow(PaymentCompletedEvent event) {
        try {
            runtimeService.createMessageCorrelation(InvoiceWorkflowVariables.PAYMENT_COMPLETED_MESSAGE)
                    .processInstanceBusinessKey(event.invoiceReference())
                    .setVariable(InvoiceWorkflowVariables.PAYMENT_SUCCESS, "SUCCESS".equalsIgnoreCase(event.status()))
                    .correlate();
            return true;
        } catch (MismatchingMessageCorrelationException ex) {
            LOGGER.info(
                    "Aucune instance Camunda en attente du paiement. invoiceId={}, reference={}, status={}",
                    event.invoiceId(),
                    event.invoiceReference(),
                    event.status()
            );
            return false;
        }
    }

    private boolean isDuplicate(String eventId) {
        return eventId != null
                && !eventId.isBlank()
                && processedMessageRepository.existsById(eventId);
    }

    private void markAsProcessed(String eventId, String eventType) {
        if (eventId != null && !eventId.isBlank()) {
            processedMessageRepository.save(new ProcessedMessage(eventId, eventType));
        }
    }
}
