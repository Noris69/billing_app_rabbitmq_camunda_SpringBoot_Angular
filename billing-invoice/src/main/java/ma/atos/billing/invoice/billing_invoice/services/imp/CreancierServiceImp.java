package ma.atos.billing.invoice.billing_invoice.services.imp;

import jakarta.persistence.criteria.Predicate;
import ma.atos.billing.invoice.billing_invoice.dtos.CreancierDto;
import ma.atos.billing.invoice.billing_invoice.dtos.CreancierSearchCriteria;
import ma.atos.billing.invoice.billing_invoice.entities.Creancier;
import ma.atos.billing.invoice.billing_invoice.exception.FunctionalException;
import ma.atos.billing.invoice.billing_invoice.exception.TechnicalException;
import ma.atos.billing.invoice.billing_invoice.mappers.CreancierMapper;
import ma.atos.billing.invoice.billing_invoice.repository.CreancierRepository;
import ma.atos.billing.invoice.billing_invoice.services.CreancierService;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CreancierServiceImp implements CreancierService {

    private final CreancierRepository repository;
    private final CreancierMapper mapper;

    public CreancierServiceImp(CreancierRepository repository, CreancierMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CreancierDto create(CreancierDto dto) {
        try {
            Creancier entity = mapper.toEntity(dto);
            return mapper.toDto(repository.save(entity));
        } catch (DataAccessException ex) {
            throw new TechnicalException("Erreur technique lors de la creation du creancier", ex);
        } catch (RuntimeException ex) {
            throw new TechnicalException("Erreur technique lors de la creation du creancier", ex);
        }
    }

    @Override
    @Transactional
    public CreancierDto update(Long id, CreancierDto dto) {
        try {
            Creancier entity = repository.findById(id)
                    .orElseThrow(() -> new FunctionalException("Creancier introuvable avec l'id : " + id, HttpStatus.NOT_FOUND));

            mapper.updateEntity(dto, entity);
            return mapper.toDto(entity);
        } catch (FunctionalException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            throw new TechnicalException("Erreur technique lors de la modification du creancier", ex);
        } catch (RuntimeException ex) {
            throw new TechnicalException("Erreur technique lors de la modification du creancier", ex);
        }
    }

    @Override
    public void delete(Long id) {
        try {
            if (!repository.existsById(id)) {
                throw new FunctionalException("Creancier introuvable avec l'id : " + id, HttpStatus.NOT_FOUND);
            }

            repository.deleteById(id);
        } catch (FunctionalException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            throw new TechnicalException("Erreur technique lors de la suppression du creancier", ex);
        } catch (RuntimeException ex) {
            throw new TechnicalException("Erreur technique lors de la suppression du creancier", ex);
        }
    }

    @Override
    public CreancierDto getById(Long id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new FunctionalException("Creancier introuvable avec l'id : " + id, HttpStatus.NOT_FOUND));
    }

    @Override
    public Page<CreancierDto> search(CreancierSearchCriteria criteria, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection(sortDir), creancierSortProperty(sortBy)));
        return repository.findAll(buildSpecification(criteria), pageable).map(mapper::toDto);
    }

    private Sort.Direction sortDirection(String sortDir) {
        return "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    private String creancierSortProperty(String sortBy) {
        return switch (sortBy) {
            case "nom", "typeCreancier", "ice", "banque", "email", "telephone", "createdDate" -> sortBy;
            default -> "id";
        };
    }

    private Specification<Creancier> buildSpecification(CreancierSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (hasText(criteria.getNom())) {
                predicates.add(cb.like(cb.lower(root.get("nom")), likeValue(criteria.getNom())));
            }
            if (criteria.getTypeCreancier() != null) {
                predicates.add(cb.equal(root.get("typeCreancier"), criteria.getTypeCreancier()));
            }
            if (hasText(criteria.getIce())) {
                predicates.add(cb.like(cb.lower(root.get("ice")), likeValue(criteria.getIce())));
            }
            if (hasText(criteria.getRc())) {
                predicates.add(cb.like(cb.lower(root.get("rc")), likeValue(criteria.getRc())));
            }
            if (hasText(criteria.getRib())) {
                predicates.add(cb.like(cb.lower(root.get("rib")), likeValue(criteria.getRib())));
            }
            if (hasText(criteria.getBanque())) {
                predicates.add(cb.like(cb.lower(root.get("banque")), likeValue(criteria.getBanque())));
            }
            if (hasText(criteria.getEmail())) {
                predicates.add(cb.like(cb.lower(root.get("email")), likeValue(criteria.getEmail())));
            }
            if (hasText(criteria.getTelephone())) {
                predicates.add(cb.like(cb.lower(root.get("telephone")), likeValue(criteria.getTelephone())));
            }
            if (hasText(criteria.getAdresse())) {
                predicates.add(cb.like(cb.lower(root.get("adresse")), likeValue(criteria.getAdresse())));
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
