package ma.atos.billing.invoice.billing_invoice.mappers;

import ma.atos.billing.invoice.billing_invoice.dtos.AgenceDto;
import ma.atos.billing.invoice.billing_invoice.dtos.DistributeurDto;
import ma.atos.billing.invoice.billing_invoice.dtos.PointDeVenteDto;
import ma.atos.billing.invoice.billing_invoice.dtos.PointDeVenteType;
import ma.atos.billing.invoice.billing_invoice.entities.Agence;
import ma.atos.billing.invoice.billing_invoice.entities.Distributeur;
import ma.atos.billing.invoice.billing_invoice.entities.PointDeVente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PointDeVenteMapper {

    default PointDeVenteDto toDto(PointDeVente entity) {
        if (entity == null) {
            return null;
        }

        if (entity instanceof Agence agence) {
            return toDto(agence);
        }

        if (entity instanceof Distributeur distributeur) {
            return toDto(distributeur);
        }

        return null;
    }

    @Mapping(target = "type", constant = "AGENCE")
    PointDeVenteDto toDto(Agence entity);

    @Mapping(target = "type", constant = "DISTRIBUTEUR")
    PointDeVenteDto toDto(Distributeur entity);

    PointDeVenteDto toPointDeVenteDto(PointDeVente pointDeVente);

    @Mapping(target = "type", constant = "AGENCE")
    AgenceDto toAgenceDto(Agence agence);

    @Mapping(target = "type", constant = "DISTRIBUTEUR")
    DistributeurDto toDistributeurDto(Distributeur distributeur);

    default PointDeVente toEntity(PointDeVenteDto dto) {
        if (dto == null) {
            return null;
        }

        return switch (dto.getType()) {
            case AGENCE -> toAgence(dto);
            case DISTRIBUTEUR -> toDistributeur(dto);
        };
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "invoices", ignore = true)
    Agence toAgence(PointDeVenteDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "invoices", ignore = true)
    Distributeur toDistributeur(PointDeVenteDto dto);

    default void updateEntity(PointDeVente entity, PointDeVenteDto dto) {
        if (entity == null || dto == null) {
            return;
        }

        if (entity instanceof Agence agence) {
            updateAgence(dto, agence);
        }

        if (entity instanceof Distributeur distributeur) {
            updateDistributeur(dto, distributeur);
        }
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "invoices", ignore = true)
    void updateAgence(PointDeVenteDto dto, @MappingTarget Agence entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "invoices", ignore = true)
    void updateDistributeur(PointDeVenteDto dto, @MappingTarget Distributeur entity);
}
