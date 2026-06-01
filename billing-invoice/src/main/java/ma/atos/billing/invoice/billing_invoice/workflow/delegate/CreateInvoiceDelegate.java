package ma.atos.billing.invoice.billing_invoice.workflow.delegate;

import ma.atos.billing.invoice.billing_invoice.entities.Creancier;
import ma.atos.billing.invoice.billing_invoice.entities.Customer;
import ma.atos.billing.invoice.billing_invoice.entities.Invoice;
import ma.atos.billing.invoice.billing_invoice.entities.PointDeVente;
import ma.atos.billing.invoice.billing_invoice.enums.ModeReglement;
import ma.atos.billing.invoice.billing_invoice.enums.StatusInvoice;
import ma.atos.billing.invoice.billing_invoice.repository.CreancierRepository;
import ma.atos.billing.invoice.billing_invoice.repository.CustomerRepository;
import ma.atos.billing.invoice.billing_invoice.repository.InvoiceRepository;
import ma.atos.billing.invoice.billing_invoice.repository.PointDeVenteRepository;
import ma.atos.billing.invoice.billing_invoice.services.InvoiceBusinessValidator;
import ma.atos.billing.invoice.billing_invoice.workflow.InvoiceWorkflowVariables;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component("createInvoiceDelegate")
public class CreateInvoiceDelegate implements JavaDelegate {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final CreancierRepository creancierRepository;
    private final PointDeVenteRepository pointDeVenteRepository;
    private final InvoiceBusinessValidator validator;

    public CreateInvoiceDelegate(
            InvoiceRepository invoiceRepository,
            CustomerRepository customerRepository,
            CreancierRepository creancierRepository,
            PointDeVenteRepository pointDeVenteRepository,
            InvoiceBusinessValidator validator
    ) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.creancierRepository = creancierRepository;
        this.pointDeVenteRepository = pointDeVenteRepository;
        this.validator = validator;
    }

    @Override
    public void execute(DelegateExecution execution) {
        Object existingInvoiceId = execution.getVariable(InvoiceWorkflowVariables.INVOICE_ID);
        if (existingInvoiceId != null) {
            return;
        }

        Long customerId = requiredLong(execution, InvoiceWorkflowVariables.CUSTOMER_ID);
        Long creancierId = requiredLong(execution, InvoiceWorkflowVariables.CREANCIER_ID);
        Long pointDeVenteId = requiredLong(execution, InvoiceWorkflowVariables.POINT_DE_VENTE_ID);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer introuvable : " + customerId));
        Creancier creancier = creancierRepository.findById(creancierId)
                .orElseThrow(() -> new IllegalArgumentException("Creancier introuvable : " + creancierId));
        PointDeVente pointDeVente = pointDeVenteRepository.findById(pointDeVenteId)
                .orElseThrow(() -> new IllegalArgumentException("Point de vente introuvable : " + pointDeVenteId));

        Invoice invoice = new Invoice();
        invoice.setReference(requiredString(execution, InvoiceWorkflowVariables.REFERENCE));
        invoice.setDateInvoice(optionalDate(execution, InvoiceWorkflowVariables.DATE_INVOICE));
        invoice.setDateDue(optionalDate(execution, InvoiceWorkflowVariables.DATE_DUE));
        invoice.setMontantHt(optionalBigDecimal(execution, InvoiceWorkflowVariables.MONTANT_HT));
        invoice.setMontantTva(optionalBigDecimal(execution, InvoiceWorkflowVariables.MONTANT_TVA));
        invoice.setMontantTtc(requiredBigDecimal(execution, InvoiceWorkflowVariables.MONTANT_TTC));
        invoice.setModeReglement(optionalModeReglement(execution));
        invoice.setDescription((String) execution.getVariable(InvoiceWorkflowVariables.DESCRIPTION));
        invoice.setStatus(StatusInvoice.EN_ATTENTE);
        invoice.setCustomer(customer);
        invoice.setCreancier(creancier);
        invoice.setPointDeVente(pointDeVente);
        validator.validate(invoice);
        ensureUniqueReference(invoice.getReference());

        Invoice savedInvoice = invoiceRepository.save(invoice);
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

    private void ensureUniqueReference(String reference) {
        invoiceRepository.findByReference(reference).ifPresent(invoice -> {
            throw new IllegalArgumentException("Reference facture deja utilisee : " + reference);
        });
    }
}
