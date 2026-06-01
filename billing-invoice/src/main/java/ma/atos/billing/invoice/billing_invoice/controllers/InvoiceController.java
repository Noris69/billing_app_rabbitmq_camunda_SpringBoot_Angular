package ma.atos.billing.invoice.billing_invoice.controllers;

import ma.atos.billing.invoice.billing_invoice.dtos.InvoiceDto;
import ma.atos.billing.invoice.billing_invoice.entities.Invoice;
import ma.atos.billing.invoice.billing_invoice.enums.StatusInvoice;
import ma.atos.billing.invoice.billing_invoice.services.InvoiceNotificationService;
import ma.atos.billing.invoice.billing_invoice.services.InvoiceReportService;
import ma.atos.billing.invoice.billing_invoice.services.InvoiceService;
import jakarta.validation.Valid;
import net.sf.jasperreports.engine.JRException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceNotificationService notificationService;
    private final InvoiceReportService reportService;

    public InvoiceController(
            InvoiceService invoiceService,
            InvoiceNotificationService notificationService,
            InvoiceReportService reportService
    ) {
        this.invoiceService = invoiceService;
        this.notificationService = notificationService;
        this.reportService = reportService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamInvoices() {
        return notificationService.subscribe();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getById(id));
    }

    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<InvoiceDto> getByIdAlias(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getById(id));
    }

    @PostMapping
    public ResponseEntity<InvoiceDto> create(@Valid @RequestBody InvoiceDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.create(dto));
    }

    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<byte[]> exportInvoiceReceiptPdf(@PathVariable Long id) {
        try {
            Invoice invoice = invoiceService.getEntityById(id);
            
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
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(invoiceService.search(
                reference,
                status,
                customerId,
                creancierId,
                pointDeVenteId,
                page,
                size,
                sortBy,
                sortDir
        ));
    }
}
