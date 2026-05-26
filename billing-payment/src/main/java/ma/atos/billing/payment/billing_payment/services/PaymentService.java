package ma.atos.billing.payment.billing_payment.services;

import ma.atos.billing.payment.billing_payment.dtos.PaymentDto;
import ma.atos.billing.payment.billing_payment.dtos.PaymentRequestDto;
import ma.atos.billing.payment.billing_payment.dtos.PaymentSearchCriteria;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PaymentService {

    PaymentDto createPayment(PaymentRequestDto request);

    PaymentDto getById(Long id);

    List<PaymentDto> findByCustomerId(Long customerId);

    default Page<PaymentDto> search(PaymentSearchCriteria criteria, int page, int size) {
        return search(criteria, page, size, "id", "desc");
    }

    Page<PaymentDto> search(PaymentSearchCriteria criteria, int page, int size, String sortBy, String sortDir);
}
