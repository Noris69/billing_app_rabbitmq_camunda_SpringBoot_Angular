package ma.atos.billing.invoice.billing_invoice.services;

import ma.atos.billing.invoice.billing_invoice.dtos.PointDeVenteDto;
import ma.atos.billing.invoice.billing_invoice.dtos.PointDeVenteSearchCriteria;
import org.springframework.data.domain.Page;

public interface PointDeventeService {

    PointDeVenteDto create(PointDeVenteDto dto);

    PointDeVenteDto update(Long id, PointDeVenteDto dto);

    void delete(Long id);

    PointDeVenteDto getPointDeVenteById(long id);

    Page<PointDeVenteDto> searchPointDeVente(PointDeVenteSearchCriteria criteria, int page, int size);
}
