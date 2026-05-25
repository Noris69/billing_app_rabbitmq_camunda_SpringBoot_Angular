package ma.atos.billing.customer.billing_customer.services;

import jakarta.persistence.criteria.Predicate;
import ma.atos.billing.customer.billing_customer.dtos.CustomerDto;
import ma.atos.billing.customer.billing_customer.dtos.CustomerSearchCriteria;
import ma.atos.billing.customer.billing_customer.entities.Customer;
import ma.atos.billing.customer.billing_customer.repositories.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImp implements CustomerService {

    private final CustomerRepository repository;

    public CustomerServiceImp(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<CustomerDto> search(CustomerSearchCriteria criteria, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findAll(buildSpecification(criteria), pageable).map(this::toDto);
    }

    private CustomerDto toDto(Customer entity) {
        return new CustomerDto(
                entity.getId(),
                entity.getNom(),
                entity.getPrenom(),
                entity.getAdresse(),
                entity.getPaymentType(),
                entity.getCreatedDate(),
                entity.getUpdatedDate()
        );
    }

    private Specification<Customer> buildSpecification(CustomerSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (hasText(criteria.getNom())) {
                predicates.add(cb.like(cb.lower(root.get("nom")), likeValue(criteria.getNom())));
            }
            if (hasText(criteria.getPrenom())) {
                predicates.add(cb.like(cb.lower(root.get("prenom")), likeValue(criteria.getPrenom())));
            }
            if (hasText(criteria.getAdresse())) {
                predicates.add(cb.like(cb.lower(root.get("adresse")), likeValue(criteria.getAdresse())));
            }
            if (hasText(criteria.getPaymentType())) {
                predicates.add(cb.like(cb.lower(root.get("paymentType")), likeValue(criteria.getPaymentType())));
            }
            if (hasText(criteria.getQuery())) {
                String value = likeValue(criteria.getQuery());
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nom")), value),
                        cb.like(cb.lower(root.get("prenom")), value),
                        cb.like(cb.lower(root.get("adresse")), value),
                        cb.like(cb.lower(root.get("paymentType")), value)
                ));
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
