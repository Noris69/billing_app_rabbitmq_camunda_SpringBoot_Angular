package ma.atos.billing.invoice.billing_invoice.services;

import ma.atos.billing.invoice.billing_invoice.dtos.CreancierDto;
import ma.atos.billing.invoice.billing_invoice.dtos.CreancierSearchCriteria;
import org.springframework.data.domain.Page;

public interface CreancierService {

    CreancierDto create(CreancierDto dto);

    CreancierDto update(Long id, CreancierDto dto);

    void delete(Long id);

    CreancierDto getById(Long id);

    Page<CreancierDto> search(CreancierSearchCriteria criteria, int page, int size);
}
