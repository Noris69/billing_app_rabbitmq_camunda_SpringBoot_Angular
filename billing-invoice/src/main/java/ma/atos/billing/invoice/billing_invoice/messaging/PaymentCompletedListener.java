package ma.atos.billing.invoice.billing_invoice.messaging;

import ma.atos.billing.invoice.billing_invoice.dtos.InvoiceDto;
import ma.atos.billing.invoice.billing_invoice.entities.Invoice;
import ma.atos.billing.invoice.billing_invoice.entities.ProcessedMessage;
import ma.atos.billing.invoice.billing_invoice.enums.StatusInvoice;
import ma.atos.billing.invoice.billing_invoice.mappers.InvoiceMapper;
import ma.atos.billing.invoice.billing_invoice.repository.InvoiceRepository;
import ma.atos.billing.invoice.billing_invoice.repository.ProcessedMessageRepository;
import ma.atos.billing.invoice.billing_invoice.services.InvoiceNotificationService;
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

    private final InvoiceRepository invoiceRepository;
    private final ProcessedMessageRepository processedMessageRepository;
    private final InvoiceNotificationService notificationService;
    private final InvoiceMapper invoiceMapper;
    private final RuntimeService runtimeService;

    public PaymentCompletedListener(
            InvoiceRepository invoiceRepository,
            ProcessedMessageRepository processedMessageRepository,
            InvoiceNotificationService notificationService,
            InvoiceMapper invoiceMapper,
            RuntimeService runtimeService
    ) {
        this.invoiceRepository = invoiceRepository;
        this.processedMessageRepository = processedMessageRepository;
        this.notificationService = notificationService;
        this.invoiceMapper = invoiceMapper;
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

        Invoice invoice = invoiceRepository.findById(event.invoiceId())
                .orElseThrow(() -> new IllegalArgumentException("Facture introuvable : " + event.invoiceId()));

        if ("SUCCESS".equalsIgnoreCase(event.status())) {
            invoice.setStatus(StatusInvoice.PAYEE);
        } else {
            invoice.setStatus(StatusInvoice.REJECTED);
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Notification temps réel SSE du changement de statut
        InvoiceDto dto = invoiceMapper.toDto(savedInvoice);
        notificationService.notifyInvoiceChange(dto);
        correlateWorkflow(event);
    }

    private void correlateWorkflow(PaymentCompletedEvent event) {
        try {
            runtimeService.createMessageCorrelation(InvoiceWorkflowVariables.PAYMENT_COMPLETED_MESSAGE)
                    .processInstanceBusinessKey(event.invoiceReference())
                    .setVariable(InvoiceWorkflowVariables.PAYMENT_SUCCESS, "SUCCESS".equalsIgnoreCase(event.status()))
                    .correlate();
        } catch (MismatchingMessageCorrelationException ex) {
            LOGGER.info(
                    "Aucune instance Camunda en attente du paiement. invoiceId={}, reference={}, status={}",
                    event.invoiceId(),
                    event.invoiceReference(),
                    event.status()
            );
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
