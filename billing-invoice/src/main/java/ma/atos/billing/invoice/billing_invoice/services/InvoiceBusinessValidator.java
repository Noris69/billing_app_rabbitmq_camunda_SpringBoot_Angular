package ma.atos.billing.invoice.billing_invoice.services;

import ma.atos.billing.invoice.billing_invoice.dtos.InvoiceDto;
import ma.atos.billing.invoice.billing_invoice.entities.Invoice;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class InvoiceBusinessValidator {

    public void validate(InvoiceDto dto) {
        requireNotNull(dto.getCustomerId(), "customerId obligatoire");
        requireNotNull(dto.getCreancierId(), "creancierId obligatoire");
        requireNotNull(dto.getPointDeVenteId(), "pointDeVenteId obligatoire");
        requireNotNull(dto.getModeReglement(), "modeReglement obligatoire");
        validateDates(dto.getDateInvoice(), dto.getDateDue());
        validateAmounts(dto.getMontantHt(), dto.getMontantTva(), dto.getMontantTtc());
    }

    public void validate(Invoice invoice) {
        requireNotNull(invoice.getCustomer(), "Customer obligatoire");
        requireNotNull(invoice.getCreancier(), "Creancier obligatoire");
        requireNotNull(invoice.getPointDeVente(), "Point de vente obligatoire");
        requireNotNull(invoice.getModeReglement(), "modeReglement obligatoire");
        validateDates(invoice.getDateInvoice(), invoice.getDateDue());
        validateAmounts(invoice.getMontantHt(), invoice.getMontantTva(), invoice.getMontantTtc());
    }

    private void validateDates(java.time.LocalDate dateInvoice, java.time.LocalDate dateDue) {
        if (dateInvoice != null && dateDue != null && dateDue.isBefore(dateInvoice)) {
            throw new IllegalArgumentException("La date d'echeance doit etre superieure ou egale a la date de facture.");
        }
    }

    private void validateAmounts(BigDecimal montantHt, BigDecimal montantTva, BigDecimal montantTtc) {
        requirePositiveOrZero(montantHt, "montantHt");
        requirePositiveOrZero(montantTva, "montantTva");
        requireStrictlyPositive(montantTtc, "montantTtc");

        BigDecimal ht = amountOrZero(montantHt);
        BigDecimal tva = amountOrZero(montantTva);
        if (montantTtc != null && ht.add(tva).compareTo(montantTtc) != 0) {
            throw new IllegalArgumentException("Le montant TTC doit etre egal au montant HT plus TVA.");
        }
    }

    private void requireNotNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private void requirePositiveOrZero(BigDecimal value, String fieldName) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " doit etre positif ou nul.");
        }
    }

    private void requireStrictlyPositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldName + " doit etre strictement positif.");
        }
    }

    private BigDecimal amountOrZero(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }
}
