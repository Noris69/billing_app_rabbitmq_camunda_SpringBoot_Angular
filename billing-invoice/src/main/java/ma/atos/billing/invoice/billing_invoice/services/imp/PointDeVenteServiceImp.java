package ma.atos.billing.invoice.billing_invoice.services.imp;

import jakarta.persistence.criteria.Predicate;
import ma.atos.billing.invoice.billing_invoice.dtos.PointDeVenteDto;
import ma.atos.billing.invoice.billing_invoice.dtos.PointDeVenteSearchCriteria;
import ma.atos.billing.invoice.billing_invoice.dtos.PointDeVenteType;
import ma.atos.billing.invoice.billing_invoice.entities.Agence;
import ma.atos.billing.invoice.billing_invoice.entities.Distributeur;
import ma.atos.billing.invoice.billing_invoice.entities.PointDeVente;
import ma.atos.billing.invoice.billing_invoice.exception.FunctionalException;
import ma.atos.billing.invoice.billing_invoice.exception.TechnicalException;
import ma.atos.billing.invoice.billing_invoice.mappers.PointDeVenteMapper;
import ma.atos.billing.invoice.billing_invoice.repository.PointDeVenteRepository;
import ma.atos.billing.invoice.billing_invoice.routes.PointDeVenteExportRoute;
import ma.atos.billing.invoice.billing_invoice.services.PointDeventeService;
import org.apache.camel.ProducerTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;
import java.util.List;

@Service
public class PointDeVenteServiceImp implements PointDeventeService {

    private final PointDeVenteRepository repository;
    private final PointDeVenteMapper mapper;
    private final ProducerTemplate producerTemplate;

    public PointDeVenteServiceImp(
            PointDeVenteRepository repository,
            PointDeVenteMapper mapper,
            ProducerTemplate producerTemplate
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.producerTemplate = producerTemplate;
    }

    @Override
    @CacheEvict(value = "pointsDeVente-list", allEntries = true)
    public PointDeVenteDto create(PointDeVenteDto dto) {
        try {
            validatePointDeVenteType(dto);
            PointDeVente entity = mapper.toEntity(dto);
            PointDeVenteDto savedPointDeVente = mapper.toDto(repository.save(entity));
            producerTemplate.sendBody(
                    PointDeVenteExportRoute.POINT_DE_VENTE_CREATED_ENDPOINT,
                    savedPointDeVente
            );
            return savedPointDeVente;
        } catch (FunctionalException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            throw new TechnicalException("Erreur technique lors de la creation du point de vente", ex);
        } catch (RuntimeException ex) {
            throw new TechnicalException("Erreur technique lors de la creation du point de vente", ex);
        }
    }

    @Override
    @Transactional
    @CachePut(value = "pointDeVente", key = "#id")
    public PointDeVenteDto update(Long id, PointDeVenteDto dto) {
        try {
            validatePointDeVenteType(dto);
            PointDeVente entity = repository.findById(id)
                    .orElseThrow(() -> new FunctionalException("Point de vente introuvable avec l'id : " + id, HttpStatus.NOT_FOUND));

            if (dto.getType() != resolveType(entity)) {
                throw new FunctionalException("Le type du point de vente ne peut pas etre modifie", HttpStatus.BAD_REQUEST);
            }

            mapper.updateEntity(entity, dto);
            return mapper.toDto(entity);
        } catch (FunctionalException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            throw new TechnicalException("Erreur technique lors de la modification du point de vente", ex);
        } catch (RuntimeException ex) {
            throw new TechnicalException("Erreur technique lors de la modification du point de vente", ex);
        }
    }

    @Override
    @CacheEvict(value = "pointDeVente", key = "#id")
    public void delete(Long id) {
        try {
            if (!repository.existsById(id)) {
                throw new FunctionalException("Point de vente introuvable avec l'id : " + id, HttpStatus.NOT_FOUND);
            }

            repository.deleteById(id);
        } catch (FunctionalException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            throw new TechnicalException("Erreur technique lors de la suppression du point de vente", ex);
        } catch (RuntimeException ex) {
            throw new TechnicalException("Erreur technique lors de la suppression du point de vente", ex);
        }
    }

    @Override
    public PointDeVenteDto getPointDeVenteById(long id) {
        PointDeVente pointDeVente = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return mapToTypedDto(pointDeVente);
    }

    @Override
    public Page<PointDeVenteDto> searchPointDeVente(PointDeVenteSearchCriteria criteria, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection(sortDir), pointDeVenteSortProperty(sortBy)));

        Specification<PointDeVente> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(criteria.getNom())) {
                predicates.add(cb.like(cb.lower(root.get("nom")), likeValue(criteria.getNom())));
            }

            if (hasText(criteria.getAdresse())) {
                predicates.add(cb.like(cb.lower(root.get("adresse")), likeValue(criteria.getAdresse())));
            }

            if (hasText(criteria.getTelephone())) {
                predicates.add(cb.like(cb.lower(root.get("telephone")), likeValue(criteria.getTelephone())));
            }

            if (hasText(criteria.getType_point_de_vente())) {
                if ("AGENCE".equalsIgnoreCase(criteria.getType_point_de_vente())) {
                    predicates.add(cb.equal(root.type(), Agence.class));
                } else if ("DISTRIBUTEUR".equalsIgnoreCase(criteria.getType_point_de_vente())) {
                    predicates.add(cb.equal(root.type(), Distributeur.class));
                }
            }

            if (hasText(criteria.getCodeAgence())) {
                predicates.add(cb.like(cb.lower(cb.treat(root, Agence.class).get("codeAgence")), likeValue(criteria.getCodeAgence())));
            }

            if (hasText(criteria.getResponsable())) {
                predicates.add(cb.like(cb.lower(cb.treat(root, Agence.class).get("responsable")), likeValue(criteria.getResponsable())));
            }

            if (hasText(criteria.getRegion())) {
                predicates.add(cb.like(cb.lower(cb.treat(root, Agence.class).get("region")), likeValue(criteria.getRegion())));
            }

            if (hasText(criteria.getTypeAgence())) {
                predicates.add(cb.like(cb.lower(cb.treat(root, Agence.class).get("typeAgence")), likeValue(criteria.getTypeAgence())));
            }

            if (hasText(criteria.getCodeDistributeur())) {
                predicates.add(cb.like(cb.lower(cb.treat(root, Distributeur.class).get("codeDistributeur")), likeValue(criteria.getCodeDistributeur())));
            }

            if (hasText(criteria.getZoneDistribution())) {
                predicates.add(cb.like(cb.lower(cb.treat(root, Distributeur.class).get("zoneDistribution")), likeValue(criteria.getZoneDistribution())));
            }

            if (hasText(criteria.getNomCommercial())) {
                predicates.add(cb.like(cb.lower(cb.treat(root, Distributeur.class).get("nomCommercial")), likeValue(criteria.getNomCommercial())));
            }

            if (criteria.getCommission() != null) {
                predicates.add(cb.equal(cb.treat(root, Distributeur.class).get("commission"), criteria.getCommission()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(spec, pageable).map(this::mapToTypedDto);
    }

    private Sort.Direction sortDirection(String sortDir) {
        return "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    private String pointDeVenteSortProperty(String sortBy) {
        return switch (sortBy) {
            case "nom", "adresse", "telephone" -> sortBy;
            default -> "id";
        };
    }

    private PointDeVenteType resolveType(PointDeVente entity) {
        if (entity instanceof Agence) {
            return PointDeVenteType.AGENCE;
        }
        if (entity instanceof Distributeur) {
            return PointDeVenteType.DISTRIBUTEUR;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    private void validatePointDeVenteType(PointDeVenteDto dto) {
        if (dto == null || dto.getType() == null) {
            throw new FunctionalException("Le type du point de vente est obligatoire", HttpStatus.BAD_REQUEST);
        }
    }

    private PointDeVenteDto mapToTypedDto(PointDeVente pointDeVente) {
        if (pointDeVente instanceof Agence agence) {
            return mapper.toAgenceDto(agence);
        }
        if (pointDeVente instanceof Distributeur distributeur) {
            return mapper.toDistributeurDto(distributeur);
        }
        return mapper.toPointDeVenteDto(pointDeVente);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String likeValue(String value) {
        return "%" + value.toLowerCase() + "%";
    }
}
