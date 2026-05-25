package ma.atos.billing.customer.billing_customer.services;

import ma.atos.billing.customer.billing_customer.dtos.CustomerDto;
import ma.atos.billing.customer.billing_customer.dtos.CustomerSearchCriteria;
import org.springframework.data.domain.Page;

public interface CustomerService {

    Page<CustomerDto> search(CustomerSearchCriteria criteria, int page, int size);
}
