package ma.atos.billing.invoice.billing_invoice.controllers;

import ma.atos.billing.invoice.billing_invoice.dtos.InvoiceDto;
import ma.atos.billing.invoice.billing_invoice.entities.Invoice;
import ma.atos.billing.invoice.billing_invoice.enums.StatusInvoice;
import ma.atos.billing.invoice.billing_invoice.mappers.InvoiceMapper;
import ma.atos.billing.invoice.billing_invoice.repository.InvoiceRepository;
import ma.atos.billing.invoice.billing_invoice.services.InvoiceNotificationService;
import ma.atos.billing.invoice.billing_invoice.services.InvoiceReportService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceRepository repository;
    private final InvoiceMapper mapper;
    private final InvoiceNotificationService notificationService;
    private final InvoiceReportService reportService;

    public InvoiceController(
            InvoiceRepository repository,
            InvoiceMapper mapper,
            InvoiceNotificationService notificationService,
            InvoiceReportService reportService
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.notificationService = notificationService;
        this.reportService = reportService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamInvoices() {
        return notificationService.subscribe();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<byte[]> exportInvoiceReceiptPdf(@PathVariable Long id) {
        try {
            Invoice invoice = repository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Facture introuvable : " + id));
            
            byte[] pdfBytes = reportService.exportInvoiceReceiptPdf(id);
            
            String filename = "recu-facture-" + invoice.getReference() + ".pdf";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (JRException | RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Erreur lors de la génération du PDF : " + e.getMessage()).getBytes());
        }
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
