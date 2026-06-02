package ma.atos.billing.invoice.billing_invoice.workflow.delegate;

import ma.atos.billing.invoice.billing_invoice.services.InvoiceService;
import ma.atos.billing.invoice.billing_invoice.workflow.InvoiceWorkflowVariables;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("markInvoicePendingDelegate")
public class MarkInvoicePendingDelegate implements JavaDelegate {

    private final InvoiceService invoiceService;

    public MarkInvoicePendingDelegate(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        Long invoiceId = ((Number) execution.getVariable(InvoiceWorkflowVariables.INVOICE_ID)).longValue();
        invoiceService.markRejected(invoiceId);
    }
}
