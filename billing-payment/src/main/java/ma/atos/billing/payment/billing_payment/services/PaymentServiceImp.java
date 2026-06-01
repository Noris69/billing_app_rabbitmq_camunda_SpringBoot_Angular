package ma.atos.billing.payment.billing_payment.services;

import jakarta.persistence.criteria.Predicate;
import ma.atos.billing.payment.billing_payment.dtos.PaymentDashboardDto;
import ma.atos.billing.payment.billing_payment.dtos.PaymentDto;
import ma.atos.billing.payment.billing_payment.dtos.PaymentRequestDto;
import ma.atos.billing.payment.billing_payment.dtos.PaymentSearchCriteria;
import ma.atos.billing.payment.billing_payment.entities.Payment;
import ma.atos.billing.payment.billing_payment.enums.ModeReglement;
import ma.atos.billing.payment.billing_payment.enums.PaymentStatus;
import ma.atos.billing.payment.billing_payment.messaging.PaymentCompletedEvent;
import ma.atos.billing.payment.billing_payment.messaging.PaymentCompletedPublisher;
import ma.atos.billing.payment.billing_payment.repositories.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImp implements PaymentService {

    private final PaymentRepository repository;
    private final PaymentCompletedPublisher publisher;

    public PaymentServiceImp(PaymentRepository repository, PaymentCompletedPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
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
        payment.setFailureReason(failureReason(status));
        payment.setAttemptNumber(nextAttemptNumber(request.invoiceId()));

        Payment savedPayment = repository.save(payment);
        PaymentDto dto = toDto(savedPayment);
        if (status != PaymentStatus.PENDING) {
            publisher.publish(toCompletedEvent(dto, request.invoiceId(), request.invoiceReference(), status));
        }
        return dto;
    }

    @Override
    public PaymentDto getById(Long id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable : " + id));
    }

    @Override
    @Transactional
    public PaymentDto retryPayment(Long id) {
        Payment original = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable : " + id));
        PaymentStatus originalStatus = toPaymentStatus(original.getStatus());
        if (originalStatus != PaymentStatus.FAILED
                && originalStatus != PaymentStatus.CANCELLED
                && originalStatus != PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Seuls les paiements echoues ou en attente peuvent etre relances.");
        }
        if (originalStatus == PaymentStatus.PENDING) {
            original.setStatus(PaymentStatus.CANCELLED.name());
            original.setFailureReason("Paiement annule par une relance depuis l interface");
            repository.save(original);
        }

        Long rootPaymentId = original.getParentPaymentId() != null ? original.getParentPaymentId() : original.getId();
        Payment retry = new Payment();
        retry.setInvoiceId(original.getInvoiceId());
        retry.setInvoiceReference(original.getInvoiceReference());
        retry.setCustomerId(original.getCustomerId());
        retry.setCreancierId(original.getCreancierId());
        retry.setPointDeVenteId(original.getPointDeVenteId());
        retry.setAmount(original.getAmount());
        retry.setOperationType(resolveModeReglement(toModeReglement(original.getOperationType())).name());
        retry.setStatus(PaymentStatus.PENDING.name());
        retry.setFailureReason(null);
        retry.setParentPaymentId(rootPaymentId);
        retry.setAttemptNumber(nextAttemptNumber(original.getInvoiceId(), rootPaymentId));

        return toDto(repository.save(retry));
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
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable : " + id));
        if (payment.getInvoiceId() != null) {
            return repository.findByInvoiceIdOrderByAttemptNumberAscIdAsc(payment.getInvoiceId()).stream()
                    .map(this::toDto)
                    .toList();
        }

        Long rootPaymentId = payment.getParentPaymentId() != null ? payment.getParentPaymentId() : payment.getId();
        List<Payment> attempts = new ArrayList<>();
        repository.findById(rootPaymentId).ifPresent(attempts::add);
        attempts.addAll(repository.findByParentPaymentIdOrderByAttemptNumberAscIdAsc(rootPaymentId));
        return attempts.stream().map(this::toDto).toList();
    }

    @Override
    public PaymentDashboardDto dashboard() {
        return new PaymentDashboardDto(
                repository.count(),
                repository.countByStatus(PaymentStatus.SUCCESS.name()),
                repository.countByStatus(PaymentStatus.FAILED.name()),
                repository.countByStatus(PaymentStatus.PENDING.name()),
                repository.countByOperationType(ModeReglement.CARTE.name()),
                repository.countByOperationType(ModeReglement.ESPECES.name()),
                repository.sumSuccessfulAmount()
        );
    }

    @Override
    public List<PaymentDto> findByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId).stream().map(this::toDto).toList();
    }

    @Override
    public Page<PaymentDto> search(PaymentSearchCriteria criteria, int page, int size, String sortBy, String sortDir) {
        return repository.findAll(
                buildSpecification(criteria),
                PageRequest.of(page, size, Sort.by(sortDirection(sortDir), paymentSortProperty(sortBy)))
        ).map(this::toDto);
    }

    private Sort.Direction sortDirection(String sortDir) {
        return "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private PaymentDto closePayment(Long id, PaymentStatus status, String failureReason) {
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable : " + id));
        PaymentStatus currentStatus = toPaymentStatus(payment.getStatus());
        if (currentStatus != PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Seul un paiement en attente peut etre cloture.");
        }

        payment.setStatus(status.name());
        payment.setFailureReason(failureReason);
        PaymentDto dto = toDto(repository.save(payment));
        if (dto.invoiceId() != null) {
            publisher.publish(toCompletedEvent(dto, dto.invoiceId(), dto.invoiceReference(), status));
        }
        return dto;
    }

    private String paymentSortProperty(String sortBy) {
        return switch (sortBy) {
            case "customerId", "invoiceId", "creancierId", "pointDeVenteId", "amount", "operationType", "status", "createdDate" -> sortBy;
            case "modeReglement" -> "operationType";
            default -> "id";
        };
    }

    private PaymentStatus resolveStatus(PaymentRequestDto request) {
        if (request.status() != null) {
            return request.status();
        }
        if (request.paymentSuccess() != null) {
            return request.paymentSuccess() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        }
        return PaymentStatus.PENDING;
    }

    private ModeReglement resolveModeReglement(ModeReglement modeReglement) {
        return modeReglement != null ? modeReglement : ModeReglement.CARTE;
    }

    private PaymentCompletedEvent toCompletedEvent(PaymentDto payment, Long invoiceId, String invoiceReference, PaymentStatus status) {
        return new PaymentCompletedEvent(
                UUID.randomUUID().toString(),
                "PaymentCompleted",
                LocalDateTime.now(),
                payment.id(),
                invoiceId,
                invoiceReference,
                "PAY-" + payment.id(),
                payment.amount(),
                payment.currency(),
                status.name(),
                failureReason(status)
        );
    }

    private PaymentDto toDto(Payment payment) {
        PaymentStatus status = toPaymentStatus(payment.getStatus());
        return new PaymentDto(
                payment.getId(),
                payment.getInvoiceId(),
                payment.getInvoiceReference(),
                payment.getCustomerId(),
                payment.getCreancierId(),
                payment.getPointDeVenteId(),
                payment.getAmount(),
                "MAD",
                toModeReglement(payment.getOperationType()),
                "PAY-" + payment.getId(),
                status,
                payment.getFailureReason() != null ? payment.getFailureReason() : failureReason(status),
                payment.getAttemptNumber(),
                payment.getParentPaymentId(),
                toDateTime(payment.getCreatedDate(), payment.getDate()),
                toDateTime(payment.getUpdatedDate(), payment.getDate())
        );
    }

    private Specification<Payment> buildSpecification(PaymentSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }
            if (criteria.getCustomerId() != null) {
                predicates.add(cb.equal(root.get("customerId"), criteria.getCustomerId()));
            }
            if (criteria.getInvoiceId() != null) {
                predicates.add(cb.equal(root.get("invoiceId"), criteria.getInvoiceId()));
            }
            if (hasText(criteria.getInvoiceReference())) {
                predicates.add(cb.like(cb.lower(root.get("invoiceReference")), likeValue(criteria.getInvoiceReference())));
            }
            if (criteria.getCreancierId() != null) {
                predicates.add(cb.equal(root.get("creancierId"), criteria.getCreancierId()));
            }
            if (criteria.getPointDeVenteId() != null) {
                predicates.add(cb.equal(root.get("pointDeVenteId"), criteria.getPointDeVenteId()));
            }
            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus().name()));
            }
            if (hasText(criteria.getOperationType())) {
                predicates.add(cb.like(cb.lower(root.get("operationType")), likeValue(criteria.getOperationType())));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String likeValue(String value) {
        return "%" + value.toLowerCase() + "%";
    }

    private ModeReglement toModeReglement(String value) {
        if (!hasText(value)) {
            return null;
        }

        try {
            return ModeReglement.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private PaymentStatus toPaymentStatus(String value) {
        if (!hasText(value)) {
            return PaymentStatus.SUCCESS;
        }

        try {
            return PaymentStatus.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return PaymentStatus.SUCCESS;
        }
    }

    private LocalDateTime toDateTime(LocalDate primaryDate, LocalDate fallbackDate) {
        LocalDate date = primaryDate != null ? primaryDate : fallbackDate;
        return date != null ? date.atStartOfDay() : null;
    }

    private int nextAttemptNumber(Long invoiceId) {
        return nextAttemptNumber(invoiceId, null);
    }

    private int nextAttemptNumber(Long invoiceId, Long rootPaymentId) {
        if (invoiceId != null) {
            return (int) repository.countByInvoiceId(invoiceId) + 1;
        }
        if (rootPaymentId != null) {
            return repository.findByParentPaymentIdOrderByAttemptNumberAscIdAsc(rootPaymentId).size() + 2;
        }
        return 1;
    }

    private String failureReason(PaymentStatus status) {
        return status == PaymentStatus.FAILED || status == PaymentStatus.CANCELLED
                ? "Paiement refuse par la simulation"
                : null;
    }
}
