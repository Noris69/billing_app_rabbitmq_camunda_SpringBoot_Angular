package ma.atos.billing.invoice.billing_invoice.controllers;

import ma.atos.billing.invoice.billing_invoice.dtos.InvoiceDto;
import ma.atos.billing.invoice.billing_invoice.entities.Creancier;
import ma.atos.billing.invoice.billing_invoice.entities.Customer;
import ma.atos.billing.invoice.billing_invoice.entities.Invoice;
import ma.atos.billing.invoice.billing_invoice.entities.PointDeVente;
import ma.atos.billing.invoice.billing_invoice.enums.StatusInvoice;
import ma.atos.billing.invoice.billing_invoice.mappers.InvoiceMapper;
import ma.atos.billing.invoice.billing_invoice.repository.CreancierRepository;
import ma.atos.billing.invoice.billing_invoice.repository.CustomerRepository;
import ma.atos.billing.invoice.billing_invoice.repository.InvoiceRepository;
import ma.atos.billing.invoice.billing_invoice.repository.PointDeVenteRepository;
import ma.atos.billing.invoice.billing_invoice.services.InvoiceNotificationService;
import ma.atos.billing.invoice.billing_invoice.services.InvoiceReportService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceRepository repository;
    private final InvoiceMapper mapper;
    private final InvoiceNotificationService notificationService;
    private final InvoiceReportService reportService;
    private final CustomerRepository customerRepository;
    private final CreancierRepository creancierRepository;
    private final PointDeVenteRepository pointDeVenteRepository;

    public InvoiceController(
            InvoiceRepository repository,
            InvoiceMapper mapper,
            InvoiceNotificationService notificationService,
            InvoiceReportService reportService,
            CustomerRepository customerRepository,
            CreancierRepository creancierRepository,
            PointDeVenteRepository pointDeVenteRepository
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.notificationService = notificationService;
        this.reportService = reportService;
        this.customerRepository = customerRepository;
        this.creancierRepository = creancierRepository;
        this.pointDeVenteRepository = pointDeVenteRepository;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamInvoices() {
        return notificationService.subscribe();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> getById(@PathVariable Long id) {
        return findInvoiceById(id);
    }

    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<InvoiceDto> getByIdAlias(@PathVariable Long id) {
        return findInvoiceById(id);
    }

    private ResponseEntity<InvoiceDto> findInvoiceById(Long id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<InvoiceDto> create(@Valid @RequestBody InvoiceDto dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId()).orElse(null);
        Creancier creancier = creancierRepository.findById(dto.getCreancierId())
                .orElseThrow(() -> new IllegalArgumentException("Creancier introuvable : " + dto.getCreancierId()));
        PointDeVente pointDeVente = pointDeVenteRepository.findById(dto.getPointDeVenteId())
                .orElseThrow(() -> new IllegalArgumentException("Point de vente introuvable : " + dto.getPointDeVenteId()));

        if (dto.getStatus() == null) {
            dto.setStatus(StatusInvoice.EN_ATTENTE);
        }
        validateBusinessRules(dto);

        Invoice savedInvoice = repository.save(mapper.toEntity(dto, customer, creancier, pointDeVente));
        InvoiceDto savedDto = mapper.toDto(savedInvoice);
        notificationService.notifyInvoiceChange(savedDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
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
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(sortDirection(sortDir), invoiceSortProperty(sortBy)));

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

    private Sort.Direction sortDirection(String sortDir) {
        return "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private String invoiceSortProperty(String sortBy) {
        return switch (sortBy) {
            case "reference", "status", "montantHt", "montantTva", "montantTtc", "modeReglement", "createdDate", "dateInvoice", "dateDue" -> sortBy;
            default -> "id";
        };
    }

    private void validateBusinessRules(InvoiceDto dto) {
        if (dto.getDateInvoice() != null
                && dto.getDateDue() != null
                && dto.getDateDue().isBefore(dto.getDateInvoice())) {
            throw new IllegalArgumentException("La date d'echeance doit etre superieure ou egale a la date de facture.");
        }

        BigDecimal montantHt = amountOrZero(dto.getMontantHt());
        BigDecimal montantTva = amountOrZero(dto.getMontantTva());
        if (dto.getMontantTtc() != null
                && montantHt.add(montantTva).compareTo(dto.getMontantTtc()) != 0) {
            throw new IllegalArgumentException("Le montant TTC doit etre egal au montant HT plus TVA.");
        }
    }

    private BigDecimal amountOrZero(BigDecimal amount) {
        return amount != null ? amount : BigDecimal.ZERO;
    }
}
