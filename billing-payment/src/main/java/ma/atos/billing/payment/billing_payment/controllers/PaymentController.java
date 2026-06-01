package ma.atos.billing.payment.billing_payment.controllers;

import jakarta.validation.Valid;
import ma.atos.billing.payment.billing_payment.dtos.PaymentDashboardDto;
import ma.atos.billing.payment.billing_payment.dtos.PaymentDto;
import ma.atos.billing.payment.billing_payment.dtos.PaymentRequestDto;
import ma.atos.billing.payment.billing_payment.dtos.PaymentSearchCriteria;
import ma.atos.billing.payment.billing_payment.services.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PaymentDto> create(@Valid @RequestBody PaymentRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPayment(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<PaymentDto> retry(@PathVariable Long id) {
        return ResponseEntity.ok(service.retryPayment(id));
    }

    @GetMapping("/{id}/attempts")
    public ResponseEntity<List<PaymentDto>> getAttempts(@PathVariable Long id) {
        return ResponseEntity.ok(service.findAttempts(id));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<PaymentDashboardDto> dashboard() {
        return ResponseEntity.ok(service.dashboard());
    }

    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<List<PaymentDto>> getByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(service.findByCustomerId(customerId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PaymentDto>> search(
            PaymentSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(service.search(criteria, page, size, sortBy, sortDir));
    }
}
