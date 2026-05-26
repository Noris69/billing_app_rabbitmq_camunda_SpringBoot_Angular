package ma.atos.billing.invoice.billing_invoice.services;

import ma.atos.billing.invoice.billing_invoice.dtos.CreancierDto;
import ma.atos.billing.invoice.billing_invoice.dtos.CreancierSearchCriteria;
import org.springframework.data.domain.Page;

public interface CreancierService {

    CreancierDto create(CreancierDto dto);

    CreancierDto update(Long id, CreancierDto dto);

    void delete(Long id);

    CreancierDto getById(Long id);

    default Page<CreancierDto> search(CreancierSearchCriteria criteria, int page, int size) {
        return search(criteria, page, size, "id", "asc");
    }

    Page<CreancierDto> search(CreancierSearchCriteria criteria, int page, int size, String sortBy, String sortDir);
}
