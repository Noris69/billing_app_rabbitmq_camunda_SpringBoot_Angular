package ma.atos.billing.invoice.billing_invoice.messaging;

import ma.atos.billing.invoice.billing_invoice.dtos.InvoiceDto;
import ma.atos.billing.invoice.billing_invoice.entities.Invoice;
import ma.atos.billing.invoice.billing_invoice.enums.StatusInvoice;
import ma.atos.billing.invoice.billing_invoice.mappers.InvoiceMapper;
import ma.atos.billing.invoice.billing_invoice.repository.InvoiceRepository;
import ma.atos.billing.invoice.billing_invoice.services.InvoiceNotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentCompletedListener {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceNotificationService notificationService;
    private final InvoiceMapper invoiceMapper;

    public PaymentCompletedListener(
            InvoiceRepository invoiceRepository,
            InvoiceNotificationService notificationService,
            InvoiceMapper invoiceMapper
    ) {
        this.invoiceRepository = invoiceRepository;
        this.notificationService = notificationService;
        this.invoiceMapper = invoiceMapper;
    }

    @Transactional
    @RabbitListener(queues = "${billing.rabbitmq.payment-completed-queue}")
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        Invoice invoice = invoiceRepository.findById(event.invoiceId())
                .orElseThrow(() -> new IllegalArgumentException("Facture introuvable : " + event.invoiceId()));

        if ("SUCCESS".equalsIgnoreCase(event.status())) {
            invoice.setStatus(StatusInvoice.PAYEE);
        } else {
            invoice.setStatus(StatusInvoice.EN_ATTENTE);
        }

        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Notification temps réel SSE du changement de statut
        InvoiceDto dto = invoiceMapper.toDto(savedInvoice);
        notificationService.notifyInvoiceChange(dto);
    }
}
