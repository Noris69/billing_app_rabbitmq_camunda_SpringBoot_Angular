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

    Page<PaymentDto> search(PaymentSearchCriteria criteria, int page, int size);
}
