package ma.atos.billing.invoice.billing_invoice.messaging;

import ma.atos.billing.invoice.billing_invoice.entities.PaymentWorkflowCorrelationLog;
import ma.atos.billing.invoice.billing_invoice.entities.ProcessedMessage;
import ma.atos.billing.invoice.billing_invoice.enums.StatusInvoice;
import ma.atos.billing.invoice.billing_invoice.repository.PaymentWorkflowCorrelationLogRepository;
import ma.atos.billing.invoice.billing_invoice.repository.ProcessedMessageRepository;
import ma.atos.billing.invoice.billing_invoice.services.InvoiceService;
import ma.atos.billing.invoice.billing_invoice.workflow.InvoiceWorkflowVariables;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class PaymentCompletedListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentCompletedListener.class);

    private final InvoiceService invoiceService;
    private final ProcessedMessageRepository processedMessageRepository;
    private final PaymentWorkflowCorrelationLogRepository correlationLogRepository;
    private final RuntimeService runtimeService;

    public PaymentCompletedListener(
            InvoiceService invoiceService,
            ProcessedMessageRepository processedMessageRepository,
            PaymentWorkflowCorrelationLogRepository correlationLogRepository,
            RuntimeService runtimeService
    ) {
        this.invoiceService = invoiceService;
        this.processedMessageRepository = processedMessageRepository;
        this.correlationLogRepository = correlationLogRepository;
        this.runtimeService = runtimeService;
    }

    @Transactional
    @RabbitListener(queues = "${billing.rabbitmq.payment-completed-queue}")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        if (isDuplicate(event.eventId())) {
            LOGGER.info("Message paiement complete deja traite ignore. eventId={}", event.eventId());
            return;
        }

        boolean correlated = correlateWorkflow(event);
        if (!correlated) {
            LOGGER.info("Aucune instance Camunda correlee. Mise a jour directe de la facture. invoiceId={}", event.invoiceId());
            updateInvoiceStatus(event);
        }

        markAsProcessed(event.eventId(), event.eventType());
    }

    private boolean correlateWorkflow(PaymentCompletedEvent event) {
        try {
            runtimeService.createMessageCorrelation(InvoiceWorkflowVariables.PAYMENT_COMPLETED_MESSAGE)
                    .processInstanceBusinessKey(event.invoiceReference())
                    .setVariable(InvoiceWorkflowVariables.PAYMENT_SUCCESS, "SUCCESS".equalsIgnoreCase(event.status()))
                    .correlate();
            saveCorrelationLog(event, "SUCCESS", null);
            return true;
        } catch (MismatchingMessageCorrelationException ex) {
            LOGGER.info(
                    "Aucune instance Camunda en attente du paiement. invoiceId={}, reference={}, status={}",
                    event.invoiceId(),
                    event.invoiceReference(),
                    event.status()
            );
            saveCorrelationLog(event, "NOT_FOUND", ex.getMessage());
            return false;
        } catch (RuntimeException ex) {
            saveCorrelationLog(event, "FAILED", ex.getMessage());
            throw ex;
        }
    }

    private void updateInvoiceStatus(PaymentCompletedEvent event) {
        StatusInvoice status = invoiceStatusFor(event.status());
        switch (status) {
            case PAYEE -> invoiceService.markPaid(event.invoiceId());
            case REJECTED -> invoiceService.markRejected(event.invoiceId());
            case PROCESSING -> invoiceService.markProcessing(event.invoiceId());
            default -> throw new IllegalArgumentException("Statut facture non supporte apres paiement : " + status);
        }
    }

    private StatusInvoice invoiceStatusFor(String paymentStatus) {
        if (paymentStatus == null || paymentStatus.isBlank()) {
            throw new IllegalArgumentException("Statut paiement obligatoire.");
        }

        return switch (paymentStatus.toUpperCase()) {
            case "SUCCESS" -> StatusInvoice.PAYEE;
            case "FAILED", "CANCELLED" -> StatusInvoice.REJECTED;
            case "PENDING" -> StatusInvoice.PROCESSING;
            default -> throw new IllegalArgumentException("Statut paiement non supporte : " + paymentStatus);
        };
    }

    private void saveCorrelationLog(PaymentCompletedEvent event, String correlationStatus, String errorMessage) {
        PaymentWorkflowCorrelationLog log = new PaymentWorkflowCorrelationLog();
        log.setEventId(event.eventId());
        log.setInvoiceId(event.invoiceId());
        log.setInvoiceReference(event.invoiceReference());
        log.setPaymentStatus(event.status());
        log.setCorrelationStatus(correlationStatus);
        log.setErrorMessage(errorMessage);
        log.setCreatedAt(LocalDateTime.now());
        correlationLogRepository.save(log);
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
