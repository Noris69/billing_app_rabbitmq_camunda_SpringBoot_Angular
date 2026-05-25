package ma.atos.billing.invoice.billing_invoice.controllers;

import ma.atos.billing.invoice.billing_invoice.dtos.InvoiceDto;
import ma.atos.billing.invoice.billing_invoice.enums.StatusInvoice;
import ma.atos.billing.invoice.billing_invoice.mappers.InvoiceMapper;
import ma.atos.billing.invoice.billing_invoice.repository.InvoiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceRepository repository;
    private final InvoiceMapper mapper;

    public InvoiceController(InvoiceRepository repository, InvoiceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @GetMapping("/search")
    public ResponseEntity<Page<InvoiceDto>> search(
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) StatusInvoice status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long creancierId,
            @RequestParam(required = false) Long pointDeVenteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        if (reference != null && !reference.isBlank()) {
            return ResponseEntity.ok(repository.findByReferenceContainingIgnoreCase(reference, pageable).map(mapper::toDto));
        }
        if (status != null) {
            return ResponseEntity.ok(repository.findByStatus(status, pageable).map(mapper::toDto));
        }
        if (customerId != null) {
            return ResponseEntity.ok(repository.findByCustomer_Id(customerId, pageable).map(mapper::toDto));
        }
        if (creancierId != null) {
            return ResponseEntity.ok(repository.findByCreancier_Id(creancierId, pageable).map(mapper::toDto));
        }
        if (pointDeVenteId != null) {
            return ResponseEntity.ok(repository.findByPointDeVente_Id(pointDeVenteId, pageable).map(mapper::toDto));
        }

        return ResponseEntity.ok(repository.findAll(pageable).map(mapper::toDto));
    }
}
