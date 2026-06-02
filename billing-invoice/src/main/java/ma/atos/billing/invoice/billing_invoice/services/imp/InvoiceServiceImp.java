package ma.atos.billing.invoice.billing_invoice.services.imp;

import ma.atos.billing.invoice.billing_invoice.dtos.InvoiceDto;
import ma.atos.billing.invoice.billing_invoice.entities.Creancier;
import ma.atos.billing.invoice.billing_invoice.entities.Customer;
import ma.atos.billing.invoice.billing_invoice.entities.Invoice;
import ma.atos.billing.invoice.billing_invoice.entities.PointDeVente;
import ma.atos.billing.invoice.billing_invoice.enums.StatusInvoice;
import ma.atos.billing.invoice.billing_invoice.exception.FunctionalException;
import ma.atos.billing.invoice.billing_invoice.mappers.InvoiceMapper;
import ma.atos.billing.invoice.billing_invoice.repository.CreancierRepository;
import ma.atos.billing.invoice.billing_invoice.repository.CustomerRepository;
import ma.atos.billing.invoice.billing_invoice.repository.InvoiceRepository;
import ma.atos.billing.invoice.billing_invoice.repository.PointDeVenteRepository;
import ma.atos.billing.invoice.billing_invoice.services.InvoiceBusinessValidator;
import ma.atos.billing.invoice.billing_invoice.services.InvoiceNotificationService;
import ma.atos.billing.invoice.billing_invoice.services.InvoiceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceServiceImp implements InvoiceService {

    private final InvoiceRepository repository;
    private final InvoiceMapper mapper;
    private final InvoiceNotificationService notificationService;
    private final CustomerRepository customerRepository;
    private final CreancierRepository creancierRepository;
    private final PointDeVenteRepository pointDeVenteRepository;
    private final InvoiceBusinessValidator validator;

    public InvoiceServiceImp(
            InvoiceRepository repository,
            InvoiceMapper mapper,
            InvoiceNotificationService notificationService,
            CustomerRepository customerRepository,
            CreancierRepository creancierRepository,
            PointDeVenteRepository pointDeVenteRepository,
            InvoiceBusinessValidator validator
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.notificationService = notificationService;
        this.customerRepository = customerRepository;
        this.creancierRepository = creancierRepository;
        this.pointDeVenteRepository = pointDeVenteRepository;
        this.validator = validator;
    }

    @Override
    public InvoiceDto getById(Long id) {
        return mapper.toDto(getEntityById(id));
    }

    @Override
    public Invoice getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new FunctionalException("Facture introuvable : " + id, HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional
    public InvoiceDto create(InvoiceDto dto) {
        return mapper.toDto(createInvoice(dto));
    }

    @Override
    @Transactional
    public Invoice createFromWorkflow(InvoiceDto dto) {
        return createInvoice(dto);
    }

    private Invoice createInvoice(InvoiceDto dto) {
        dto.setStatus(StatusInvoice.EN_ATTENTE);
        validator.validate(dto);
        ensureUniqueReference(dto.getReference());

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer introuvable : " + dto.getCustomerId()));
        Creancier creancier = creancierRepository.findById(dto.getCreancierId())
                .orElseThrow(() -> new IllegalArgumentException("Creancier introuvable : " + dto.getCreancierId()));
        PointDeVente pointDeVente = pointDeVenteRepository.findById(dto.getPointDeVenteId())
                .orElseThrow(() -> new IllegalArgumentException("Point de vente introuvable : " + dto.getPointDeVenteId()));

        Invoice invoice = mapper.toEntity(dto, customer, creancier, pointDeVente);
        invoice.setStatus(StatusInvoice.EN_ATTENTE);

        Invoice savedInvoice = repository.save(invoice);
        InvoiceDto savedDto = mapper.toDto(savedInvoice);
        notificationService.notifyInvoiceChange(savedDto);
        return savedInvoice;
    }

    @Override
    public Page<InvoiceDto> search(
            String reference,
            StatusInvoice status,
            Long customerId,
            Long creancierId,
            Long pointDeVenteId,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(sortDirection(sortDir), invoiceSortProperty(sortBy)));

        if (reference != null && !reference.isBlank()) {
            return repository.findByReferenceContainingIgnoreCase(reference, pageable).map(mapper::toDto);
        }
        if (status != null) {
            return repository.findByStatus(status, pageable).map(mapper::toDto);
        }
        if (customerId != null) {
            return repository.findByCustomer_Id(customerId, pageable).map(mapper::toDto);
        }
        if (creancierId != null) {
            return repository.findByCreancier_Id(creancierId, pageable).map(mapper::toDto);
        }
        if (pointDeVenteId != null) {
            return repository.findByPointDeVente_Id(pointDeVenteId, pageable).map(mapper::toDto);
        }

        return repository.findAll(pageable).map(mapper::toDto);
    }

    private void ensureUniqueReference(String reference) {
        repository.findByReference(reference).ifPresent(invoice -> {
            throw new IllegalArgumentException("Reference facture deja utilisee : " + reference);
        });
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

    @Override
    @Transactional
    public void markProcessing(Long id) {
        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new FunctionalException("Facture introuvable : " + id, HttpStatus.NOT_FOUND));
        invoice.setStatus(StatusInvoice.PROCESSING);
        Invoice savedInvoice = repository.save(invoice);
        notificationService.notifyInvoiceChange(mapper.toDto(savedInvoice));
    }

    @Override
    @Transactional
    public void markPaid(Long id) {
        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new FunctionalException("Facture introuvable : " + id, HttpStatus.NOT_FOUND));
        invoice.setStatus(StatusInvoice.PAYEE);
        Invoice savedInvoice = repository.save(invoice);
        notificationService.notifyInvoiceChange(mapper.toDto(savedInvoice));
    }

    @Override
    @Transactional
    public void markRejected(Long id) {
        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new FunctionalException("Facture introuvable : " + id, HttpStatus.NOT_FOUND));
        invoice.setStatus(StatusInvoice.REJECTED);
        Invoice savedInvoice = repository.save(invoice);
        notificationService.notifyInvoiceChange(mapper.toDto(savedInvoice));
    }
}
