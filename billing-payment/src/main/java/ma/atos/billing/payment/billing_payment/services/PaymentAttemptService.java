package ma.atos.billing.payment.billing_payment.services;

import ma.atos.billing.payment.billing_payment.dtos.PaymentDto;
import ma.atos.billing.payment.billing_payment.entities.Payment;
import ma.atos.billing.payment.billing_payment.mappers.PaymentMapper;
import ma.atos.billing.payment.billing_payment.repositories.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentAttemptService {

    private final PaymentRepository repository;
    private final PaymentMapper mapper;

    public PaymentAttemptService(PaymentRepository repository, PaymentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<PaymentDto> findAttempts(Long id) {
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paiement introuvable : " + id));
        if (payment.getInvoiceId() != null) {
            return repository.findByInvoiceIdOrderByAttemptNumberAscIdAsc(payment.getInvoiceId()).stream()
                    .map(mapper::toDto)
                    .toList();
        }

        Long rootPaymentId = rootPaymentId(payment);
        List<Payment> attempts = new ArrayList<>();
        repository.findById(rootPaymentId).ifPresent(attempts::add);
        attempts.addAll(repository.findByParentPaymentIdOrderByAttemptNumberAscIdAsc(rootPaymentId));
        return attempts.stream().map(mapper::toDto).toList();
    }

    public int nextAttemptNumber(Long invoiceId) {
        return nextAttemptNumber(invoiceId, null);
    }

    public int nextAttemptNumber(Long invoiceId, Long rootPaymentId) {
        if (invoiceId != null) {
            return (int) repository.countByInvoiceId(invoiceId) + 1;
        }
        if (rootPaymentId != null) {
            return repository.findByParentPaymentIdOrderByAttemptNumberAscIdAsc(rootPaymentId).size() + 2;
        }
        return 1;
    }

    public Long rootPaymentId(Payment payment) {
        return payment.getParentPaymentId() != null ? payment.getParentPaymentId() : payment.getId();
    }
}
