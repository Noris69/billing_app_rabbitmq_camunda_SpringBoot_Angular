package ma.atos.billing.invoice.billing_invoice.services;

import ma.atos.billing.invoice.billing_invoice.dtos.InvoiceDto;
import ma.atos.billing.invoice.billing_invoice.entities.Invoice;
import ma.atos.billing.invoice.billing_invoice.enums.StatusInvoice;
import org.springframework.data.domain.Page;

public interface InvoiceService {

    InvoiceDto getById(Long id);

    Invoice getEntityById(Long id);

    InvoiceDto create(InvoiceDto dto);

    Invoice createFromWorkflow(InvoiceDto dto);

    Page<InvoiceDto> search(
            String reference,
            StatusInvoice status,
            Long customerId,
            Long creancierId,
            Long pointDeVenteId,
            int page,
            int size,
            String sortBy,
            String sortDir
    );
}
