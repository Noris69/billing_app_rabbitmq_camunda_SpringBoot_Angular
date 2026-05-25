package ma.atos.billing.invoice.billing_invoice.messaging;

import ma.atos.billing.invoice.billing_invoice.entities.Invoice;
import ma.atos.billing.invoice.billing_invoice.enums.StatusInvoice;
import ma.atos.billing.invoice.billing_invoice.repository.InvoiceRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentCompletedListener {

    private final InvoiceRepository invoiceRepository;

    public PaymentCompletedListener(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
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
    }
}
