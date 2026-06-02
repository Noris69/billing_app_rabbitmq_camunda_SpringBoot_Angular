package ma.atos.billing.payment.billing_payment.messaging;

import ma.atos.billing.payment.billing_payment.dtos.PaymentRequestDto;
import ma.atos.billing.payment.billing_payment.entities.ProcessedMessage;
import ma.atos.billing.payment.billing_payment.enums.ModeReglement;
import ma.atos.billing.payment.billing_payment.repositories.ProcessedMessageRepository;
import ma.atos.billing.payment.billing_payment.services.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentRequestedListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentRequestedListener.class);

    private final PaymentService paymentService;
    private final ProcessedMessageRepository processedMessageRepository;

    public PaymentRequestedListener(
            PaymentService paymentService,
            ProcessedMessageRepository processedMessageRepository
    ) {
        this.paymentService = paymentService;
        this.processedMessageRepository = processedMessageRepository;
    }

    @Transactional
    @RabbitListener(queues = "${billing.rabbitmq.payment-requested-queue}")
    public void onPaymentRequested(PaymentRequestedEvent event) {
        if (isDuplicate(event.eventId())) {
            LOGGER.info("Message paiement deja traite ignore. eventId={}", event.eventId());
            return;
        }

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
                    null,
                    null
            ));
            markAsProcessed(event.eventId(), event.eventType());
        } catch (DataIntegrityViolationException ex) {
            LOGGER.error(
                    "Erreur contrainte DB lors du traitement du paiement. invoiceId={}, reference={}, customerId={}, creancierId={}, pointDeVenteId={}",
                    event.invoiceId(),
                    event.invoiceReference(),
                    event.customerId(),
                    event.creancierId(),
                    event.pointDeVenteId(),
                    ex
            );
            throw ex;
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

    private boolean isDuplicate(String eventId) {
        return eventId != null
                && !eventId.isBlank()
                && processedMessageRepository.existsById(eventId);
    }

    private void markAsProcessed(String eventId, String eventType) {
        if (eventId != null && !eventId.isBlank()) {
            processedMessageRepository.save(new ProcessedMessage(eventId, eventType));
        }
    }
}
