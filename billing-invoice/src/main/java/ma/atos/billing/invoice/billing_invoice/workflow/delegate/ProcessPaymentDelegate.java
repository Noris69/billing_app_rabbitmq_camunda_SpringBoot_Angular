package ma.atos.billing.invoice.billing_invoice.workflow.delegate;

import ma.atos.billing.invoice.billing_invoice.workflow.InvoiceWorkflowVariables;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("processPaymentDelegate")
public class ProcessPaymentDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        Object paymentSuccess = execution.getVariable(InvoiceWorkflowVariables.PAYMENT_SUCCESS);
        execution.setVariable(InvoiceWorkflowVariables.PAYMENT_SUCCESS, Boolean.TRUE.equals(paymentSuccess));
    }
}
