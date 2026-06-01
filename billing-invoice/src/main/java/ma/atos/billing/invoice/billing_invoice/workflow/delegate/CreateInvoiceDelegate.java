package ma.atos.billing.invoice.billing_invoice.workflow.delegate;

import ma.atos.billing.invoice.billing_invoice.dtos.InvoiceDto;
import ma.atos.billing.invoice.billing_invoice.entities.Invoice;
import ma.atos.billing.invoice.billing_invoice.enums.ModeReglement;
import ma.atos.billing.invoice.billing_invoice.services.InvoiceService;
import ma.atos.billing.invoice.billing_invoice.workflow.InvoiceWorkflowVariables;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component("createInvoiceDelegate")
public class CreateInvoiceDelegate implements JavaDelegate {

    private final InvoiceService invoiceService;

    public CreateInvoiceDelegate(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        Object existingInvoiceId = execution.getVariable(InvoiceWorkflowVariables.INVOICE_ID);
        if (existingInvoiceId != null) {
            return;
        }

        InvoiceDto invoice = new InvoiceDto();
        invoice.setReference(requiredString(execution, InvoiceWorkflowVariables.REFERENCE));
        invoice.setDateInvoice(optionalDate(execution, InvoiceWorkflowVariables.DATE_INVOICE));
        invoice.setDateDue(optionalDate(execution, InvoiceWorkflowVariables.DATE_DUE));
        invoice.setMontantHt(optionalBigDecimal(execution, InvoiceWorkflowVariables.MONTANT_HT));
        invoice.setMontantTva(optionalBigDecimal(execution, InvoiceWorkflowVariables.MONTANT_TVA));
        invoice.setMontantTtc(requiredBigDecimal(execution, InvoiceWorkflowVariables.MONTANT_TTC));
        invoice.setModeReglement(optionalModeReglement(execution));
        invoice.setDescription((String) execution.getVariable(InvoiceWorkflowVariables.DESCRIPTION));
        invoice.setCustomerId(requiredLong(execution, InvoiceWorkflowVariables.CUSTOMER_ID));
        invoice.setCreancierId(requiredLong(execution, InvoiceWorkflowVariables.CREANCIER_ID));
        invoice.setPointDeVenteId(requiredLong(execution, InvoiceWorkflowVariables.POINT_DE_VENTE_ID));

        Invoice savedInvoice = invoiceService.createFromWorkflow(invoice);
        execution.setVariable(InvoiceWorkflowVariables.INVOICE_ID, savedInvoice.getId());
    }

    private String requiredString(DelegateExecution execution, String variableName) {
        Object value = execution.getVariable(variableName);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("Variable obligatoire manquante : " + variableName);
        }
        return value.toString();
    }

    private Long requiredLong(DelegateExecution execution, String variableName) {
        Object value = execution.getVariable(variableName);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value != null) {
            return Long.valueOf(value.toString());
        }
        throw new IllegalArgumentException("Variable obligatoire manquante : " + variableName);
    }

    private BigDecimal requiredBigDecimal(DelegateExecution execution, String variableName) {
        BigDecimal value = optionalBigDecimal(execution, variableName);
        if (value == null) {
            throw new IllegalArgumentException("Variable obligatoire manquante : " + variableName);
        }
        return value;
    }

    private BigDecimal optionalBigDecimal(DelegateExecution execution, String variableName) {
        Object value = execution.getVariable(variableName);
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return value != null ? new BigDecimal(value.toString()) : null;
    }

    private LocalDate optionalDate(DelegateExecution execution, String variableName) {
        Object value = execution.getVariable(variableName);
        return value != null ? LocalDate.parse(value.toString()) : null;
    }

    private ModeReglement optionalModeReglement(DelegateExecution execution) {
        Object value = execution.getVariable(InvoiceWorkflowVariables.MODE_REGLEMENT);
        return value != null ? ModeReglement.valueOf(value.toString()) : null;
    }

}
