package ma.atos.billing.payment.billing_payment.services;

import jakarta.persistence.criteria.Predicate;
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
        payment.setCustomerId(request.customerId());
        payment.setCreancierId(request.creancierId());
        payment.setPointDeVenteId(request.pointDeVenteId());
        payment.setAmount(request.amount());
        payment.setOperationType(resolveModeReglement(request.modeReglement()).name());
        payment.setStatus(resolveStatus(request).name());

        Payment savedPayment = repository.save(payment);
        PaymentDto dto = toDto(savedPayment);
        publisher.publish(toCompletedEvent(dto, request));
        return dto;
    }

    @Override
    public PaymentDto getById(Long id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable : " + id));
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

    private String paymentSortProperty(String sortBy) {
        return switch (sortBy) {
            case "customerId", "creancierId", "pointDeVenteId", "amount", "operationType", "createdDate" -> sortBy;
            case "modeReglement" -> "operationType";
            case "status" -> "amount";
            default -> "id";
        };
    }

    private PaymentStatus resolveStatus(PaymentRequestDto request) {
        if (request.paymentSuccess() != null) {
            return request.paymentSuccess() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        }
        return request.amount() != null && request.amount() > 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
    }

    private ModeReglement resolveModeReglement(ModeReglement modeReglement) {
        return modeReglement != null ? modeReglement : ModeReglement.CARTE;
    }

    private PaymentCompletedEvent toCompletedEvent(PaymentDto payment, PaymentRequestDto request) {
        PaymentStatus status = resolveStatus(request);
        return new PaymentCompletedEvent(
                UUID.randomUUID().toString(),
                "PaymentCompleted",
                LocalDateTime.now(),
                payment.id(),
                request.invoiceId(),
                request.invoiceReference(),
                "PAY-" + payment.id(),
                payment.amount(),
                payment.currency(),
                status.name(),
                status == PaymentStatus.SUCCESS ? null : "Paiement refuse par la simulation"
        );
    }

    private PaymentDto toDto(Payment payment) {
        return new PaymentDto(
                payment.getId(),
                null,
                null,
                payment.getCustomerId(),
                payment.getCreancierId(),
                payment.getPointDeVenteId(),
                payment.getAmount(),
                "MAD",
                toModeReglement(payment.getOperationType()),
                "PAY-" + payment.getId(),
                toPaymentStatus(payment.getStatus()),
                toPaymentStatus(payment.getStatus()) == PaymentStatus.SUCCESS ? null : "Paiement refuse par la simulation",
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
            if (criteria.getCreancierId() != null) {
                predicates.add(cb.equal(root.get("creancierId"), criteria.getCreancierId()));
            }
            if (criteria.getPointDeVenteId() != null) {
                predicates.add(cb.equal(root.get("pointDeVenteId"), criteria.getPointDeVenteId()));
            }
            if (criteria.getStatus() != null) {
                if (criteria.getStatus() == PaymentStatus.SUCCESS) {
                    predicates.add(cb.greaterThan(root.get("amount"), 0));
                } else if (criteria.getStatus() == PaymentStatus.FAILED) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("amount"), 0));
                }
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
}
