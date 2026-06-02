package ma.atos.billing.payment.billing_payment.services;

import ma.atos.billing.payment.billing_payment.dtos.PaymentDashboardDto;
import ma.atos.billing.payment.billing_payment.enums.ModeReglement;
import ma.atos.billing.payment.billing_payment.enums.PaymentStatus;
import ma.atos.billing.payment.billing_payment.repositories.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentDashboardService {

    private final PaymentRepository repository;

    public PaymentDashboardService(PaymentRepository repository) {
        this.repository = repository;
    }

    public PaymentDashboardDto dashboard() {
        return new PaymentDashboardDto(
                repository.count(),
                repository.countByStatus(PaymentStatus.SUCCESS.name()),
                repository.countByStatus(PaymentStatus.FAILED.name()),
                repository.countByStatus(PaymentStatus.PENDING.name()),
                repository.countByOperationType(ModeReglement.CARTE.name()),
                repository.countByOperationType(ModeReglement.ESPECES.name()),
                repository.sumSuccessfulAmount()
        );
    }
}
