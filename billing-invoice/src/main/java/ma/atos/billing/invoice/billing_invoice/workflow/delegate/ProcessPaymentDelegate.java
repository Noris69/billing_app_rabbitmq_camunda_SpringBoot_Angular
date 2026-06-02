package ma.atos.billing.invoice.billing_invoice.workflow.delegate;

import ma.atos.billing.invoice.billing_invoice.entities.Invoice;
import ma.atos.billing.invoice.billing_invoice.messaging.PaymentRequestedPublisher;
import ma.atos.billing.invoice.billing_invoice.services.InvoiceService;
import ma.atos.billing.invoice.billing_invoice.workflow.InvoiceWorkflowVariables;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("processPaymentDelegate")
public class ProcessPaymentDelegate implements JavaDelegate {

    private final InvoiceService invoiceService;
    private final PaymentRequestedPublisher paymentRequestedPublisher;

    public ProcessPaymentDelegate(
            InvoiceService invoiceService,
            PaymentRequestedPublisher paymentRequestedPublisher
    ) {
        this.invoiceService = invoiceService;
        this.paymentRequestedPublisher = paymentRequestedPublisher;
    }

    @Override
    public void execute(DelegateExecution execution) {
        Long invoiceId = ((Number) execution.getVariable(InvoiceWorkflowVariables.INVOICE_ID)).longValue();
        invoiceService.markProcessing(invoiceId);

        Invoice invoice = invoiceService.getEntityById(invoiceId);

        Object paymentSuccess = execution.getVariable(InvoiceWorkflowVariables.PAYMENT_SUCCESS);
        Boolean requestedPaymentSuccess = paymentSuccess instanceof Boolean value ? value : null;
        paymentRequestedPublisher.publish(
                invoice,
                invoice.getCustomer() != null ? invoice.getCustomer().getId() : null,
                invoice.getCreancier() != null ? invoice.getCreancier().getId() : null,
                invoice.getPointDeVente() != null ? invoice.getPointDeVente().getId() : null,
                requestedPaymentSuccess
        );
    }
}
