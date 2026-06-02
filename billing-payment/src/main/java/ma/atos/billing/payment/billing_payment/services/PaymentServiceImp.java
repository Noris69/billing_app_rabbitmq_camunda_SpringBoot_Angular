package ma.atos.billing.payment.billing_payment.services;

import ma.atos.billing.payment.billing_payment.dtos.PaymentDashboardDto;
import ma.atos.billing.payment.billing_payment.dtos.PaymentDto;
import ma.atos.billing.payment.billing_payment.dtos.PaymentRequestDto;
import ma.atos.billing.payment.billing_payment.dtos.PaymentSearchCriteria;
import ma.atos.billing.payment.billing_payment.entities.Payment;
import ma.atos.billing.payment.billing_payment.enums.ModeReglement;
import ma.atos.billing.payment.billing_payment.enums.PaymentStatus;
import ma.atos.billing.payment.billing_payment.mappers.PaymentMapper;
import ma.atos.billing.payment.billing_payment.messaging.PaymentCompletedPublisher;
import ma.atos.billing.payment.billing_payment.repositories.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentServiceImp implements PaymentService {

    private final PaymentRepository repository;
    private final PaymentCompletedPublisher publisher;
    private final PaymentMapper mapper;
    private final PaymentAttemptService attemptService;
    private final PaymentDashboardService dashboardService;
    private final PaymentEventFactory eventFactory;
    private final PaymentSearchService searchService;

    public PaymentServiceImp(
            PaymentRepository repository,
            PaymentCompletedPublisher publisher,
            PaymentMapper mapper,
            PaymentAttemptService attemptService,
            PaymentDashboardService dashboardService,
            PaymentEventFactory eventFactory,
            PaymentSearchService searchService
    ) {
        this.repository = repository;
        this.publisher = publisher;
        this.mapper = mapper;
        this.attemptService = attemptService;
        this.dashboardService = dashboardService;
        this.eventFactory = eventFactory;
        this.searchService = searchService;
    }

    @Override
    @Transactional
    public PaymentDto createPayment(PaymentRequestDto request) {
        Payment payment = new Payment();
        payment.setInvoiceId(request.invoiceId());
        payment.setInvoiceReference(request.invoiceReference());
        payment.setCustomerId(request.customerId());
        payment.setCreancierId(request.creancierId());
        payment.setPointDeVenteId(request.pointDeVenteId());
        payment.setAmount(request.amount());
        payment.setOperationType(resolveModeReglement(request.modeReglement()).name());
        PaymentStatus status = resolveStatus(request);
        payment.setStatus(status.name());
        payment.setFailureReason(mapper.failureReason(status));
        payment.setAttemptNumber(attemptService.nextAttemptNumber(request.invoiceId()));

        Payment savedPayment = repository.save(payment);
        PaymentDto dto = mapper.toDto(savedPayment);
        if (status != PaymentStatus.PENDING) {
            publisher.publish(eventFactory.completedEvent(dto, request.invoiceId(), request.invoiceReference(), status));
        }
        return dto;
    }

    @Override
    public PaymentDto getById(Long id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable : " + id));
    }

    @Override
    @Transactional
    public PaymentDto retryPayment(Long id) {
        Payment original = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable : " + id));
        PaymentStatus originalStatus = mapper.toPaymentStatus(original.getStatus());
        if (originalStatus != PaymentStatus.FAILED
                && originalStatus != PaymentStatus.CANCELLED) {
            throw new IllegalArgumentException("Seuls les paiements echoues ou annules peuvent etre relances.");
        }

        Long rootPaymentId = attemptService.rootPaymentId(original);
        Payment retry = new Payment();
        retry.setInvoiceId(original.getInvoiceId());
        retry.setInvoiceReference(original.getInvoiceReference());
        retry.setCustomerId(original.getCustomerId());
        retry.setCreancierId(original.getCreancierId());
        retry.setPointDeVenteId(original.getPointDeVenteId());
        retry.setAmount(original.getAmount());
        retry.setOperationType(resolveModeReglement(mapper.toModeReglement(original.getOperationType())).name());
        retry.setStatus(PaymentStatus.PENDING.name());
        retry.setFailureReason(null);
        retry.setParentPaymentId(rootPaymentId);
        retry.setAttemptNumber(attemptService.nextAttemptNumber(original.getInvoiceId(), rootPaymentId));

        return mapper.toDto(repository.save(retry));
    }

    @Override
    @Transactional
    public PaymentDto markSuccess(Long id) {
        return closePayment(id, PaymentStatus.SUCCESS, null);
    }

    @Override
    @Transactional
    public PaymentDto markFailed(Long id) {
        return closePayment(id, PaymentStatus.FAILED, "Paiement refuse par la simulation");
    }

    @Override
    public List<PaymentDto> findAttempts(Long id) {
        return attemptService.findAttempts(id);
    }

    @Override
    public PaymentDashboardDto dashboard() {
        return dashboardService.dashboard();
    }

    @Override
    public List<PaymentDto> findByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId).stream().map(mapper::toDto).toList();
    }

    @Override
    public Page<PaymentDto> search(PaymentSearchCriteria criteria, int page, int size, String sortBy, String sortDir) {
        return searchService.search(criteria, page, size, sortBy, sortDir);
    }

    private PaymentDto closePayment(Long id, PaymentStatus status, String failureReason) {
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable : " + id));
        PaymentStatus currentStatus = mapper.toPaymentStatus(payment.getStatus());
        if (currentStatus != PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Seul un paiement en attente peut etre cloture.");
        }

        payment.setStatus(status.name());
        payment.setFailureReason(failureReason);
        PaymentDto dto = mapper.toDto(repository.save(payment));
        if (dto.invoiceId() != null) {
            publisher.publish(eventFactory.completedEvent(dto, dto.invoiceId(), dto.invoiceReference(), status));
        }
        return dto;
    }

    private PaymentStatus resolveStatus(PaymentRequestDto request) {
        return PaymentStatus.PENDING;
    }

    private ModeReglement resolveModeReglement(ModeReglement modeReglement) {
        if (modeReglement == null) {
            throw new IllegalArgumentException("Le mode de reglement est obligatoire.");
        }
        return modeReglement;
    }
}
