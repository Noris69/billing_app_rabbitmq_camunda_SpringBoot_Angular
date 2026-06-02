package ma.atos.billing.payment.billing_payment.services;

import jakarta.persistence.criteria.Predicate;
import ma.atos.billing.payment.billing_payment.dtos.PaymentDto;
import ma.atos.billing.payment.billing_payment.dtos.PaymentSearchCriteria;
import ma.atos.billing.payment.billing_payment.entities.Payment;
import ma.atos.billing.payment.billing_payment.mappers.PaymentMapper;
import ma.atos.billing.payment.billing_payment.repositories.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentSearchService {

    private final PaymentRepository repository;
    private final PaymentMapper mapper;

    public PaymentSearchService(PaymentRepository repository, PaymentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Page<PaymentDto> search(PaymentSearchCriteria criteria, int page, int size, String sortBy, String sortDir) {
        return repository.findAll(
                buildSpecification(criteria),
                PageRequest.of(page, size, Sort.by(sortDirection(sortDir), paymentSortProperty(sortBy)))
        ).map(mapper::toDto);
    }

    private Sort.Direction sortDirection(String sortDir) {
        return "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }

    private String paymentSortProperty(String sortBy) {
        return switch (sortBy) {
            case "customerId", "invoiceId", "creancierId", "pointDeVenteId", "amount", "operationType", "status", "createdDate" -> sortBy;
            case "modeReglement" -> "operationType";
            default -> "id";
        };
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
}
