package ma.atos.billing.customer.billing_customer.controllers;

import ma.atos.billing.customer.billing_customer.dtos.CustomerDto;
import ma.atos.billing.customer.billing_customer.dtos.CustomerSearchCriteria;
import ma.atos.billing.customer.billing_customer.services.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CustomerDto>> search(
            CustomerSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(service.search(criteria, page, size));
    }
}
