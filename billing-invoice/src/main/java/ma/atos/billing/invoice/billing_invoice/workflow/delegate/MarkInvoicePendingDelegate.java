package ma.atos.billing.invoice.billing_invoice.workflow.delegate;

import ma.atos.billing.invoice.billing_invoice.entities.Invoice;
import ma.atos.billing.invoice.billing_invoice.enums.StatusInvoice;
import ma.atos.billing.invoice.billing_invoice.repository.InvoiceRepository;
import ma.atos.billing.invoice.billing_invoice.workflow.InvoiceWorkflowVariables;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("markInvoicePendingDelegate")
public class MarkInvoicePendingDelegate implements JavaDelegate {

    private final InvoiceRepository invoiceRepository;

    public MarkInvoicePendingDelegate(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public void execute(DelegateExecution execution) {
        Long invoiceId = ((Number) execution.getVariable(InvoiceWorkflowVariables.INVOICE_ID)).longValue();
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Facture introuvable : " + invoiceId));
        invoice.setStatus(StatusInvoice.EN_ATTENTE);
        invoiceRepository.save(invoice);
    }
}
