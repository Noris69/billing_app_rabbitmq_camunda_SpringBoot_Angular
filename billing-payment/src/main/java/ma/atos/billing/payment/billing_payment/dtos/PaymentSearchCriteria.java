package ma.atos.billing.payment.billing_payment.dtos;

import ma.atos.billing.payment.billing_payment.enums.PaymentStatus;

public class PaymentSearchCriteria {

    private Long customerId;
    private Long invoiceId;
    private String invoiceReference;
    private Long creancierId;
    private Long pointDeVenteId;
    private PaymentStatus status;
    private String operationType;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceReference() {
        return invoiceReference;
    }

    public void setInvoiceReference(String invoiceReference) {
        this.invoiceReference = invoiceReference;
    }

    public Long getCreancierId() {
        return creancierId;
    }

    public void setCreancierId(Long creancierId) {
        this.creancierId = creancierId;
    }

    public Long getPointDeVenteId() {
        return pointDeVenteId;
    }

    public void setPointDeVenteId(Long pointDeVenteId) {
        this.pointDeVenteId = pointDeVenteId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
}
