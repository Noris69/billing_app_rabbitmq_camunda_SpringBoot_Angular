package ma.atos.billing.invoice.billing_invoice.dtos;

public class InvoiceWorkflowResponse {

    private final String processInstanceId;
    private final Long invoiceId;
    private final String businessKey;

    public InvoiceWorkflowResponse(String processInstanceId, Long invoiceId, String businessKey) {
        this.processInstanceId = processInstanceId;
        this.invoiceId = invoiceId;
        this.businessKey = businessKey;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public String getBusinessKey() {
        return businessKey;
    }
}
