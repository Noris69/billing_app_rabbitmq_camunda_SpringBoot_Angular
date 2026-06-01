package ma.atos.billing.payment.billing_payment.messaging;

import ma.atos.billing.payment.billing_payment.dtos.PaymentRequestDto;
import ma.atos.billing.payment.billing_payment.enums.ModeReglement;
import ma.atos.billing.payment.billing_payment.services.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestedListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentRequestedListener.class);

    private final PaymentService paymentService;

    public PaymentRequestedListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @RabbitListener(queues = "${billing.rabbitmq.payment-requested-queue}")
    public void onPaymentRequested(PaymentRequestedEvent event) {
        try {
            paymentService.createPayment(new PaymentRequestDto(
                    event.invoiceId(),
                    event.invoiceReference(),
                    event.customerId(),
                    event.creancierId(),
                    event.pointDeVenteId(),
                    event.amount(),
                    event.currency(),
                    toModeReglement(event.modeReglement()),
                    event.description(),
                    event.paymentSuccess(),
                    null
            ));
        } catch (DataIntegrityViolationException ex) {
            LOGGER.error(
                    "Message paiement ignore car il ne respecte pas les contraintes DB. invoiceId={}, reference={}, customerId={}, creancierId={}, pointDeVenteId={}",
                    event.invoiceId(),
                    event.invoiceReference(),
                    event.customerId(),
                    event.creancierId(),
                    event.pointDeVenteId(),
                    ex
            );
        } catch (RuntimeException ex) {
            LOGGER.error(
                    "Erreur lors du traitement du paiement RabbitMQ. invoiceId={}, reference={}, amount={}, modeReglement={}",
                    event.invoiceId(),
                    event.invoiceReference(),
                    event.amount(),
                    event.modeReglement(),
                    ex
            );
            throw ex;
        }
    }

    private ModeReglement toModeReglement(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return ModeReglement.valueOf(value);
    }
}
