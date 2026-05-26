package ma.atos.billing.invoice.billing_invoice.services;

import ma.atos.billing.invoice.billing_invoice.dtos.InvoiceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class InvoiceNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceNotificationService.class);
    
    // Liste thread-safe pour gérer les clients abonnés aux notifications SSE
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * Permet à un client de s'abonner au flux d'événements SSE.
     * Le timeout est fixé à 30 minutes (1 800 000 ms) pour éviter les déconnexions trop fréquentes.
     */
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(1800_000L); // 30 minutes
        
        this.emitters.add(emitter);

        emitter.onCompletion(() -> {
            LOGGER.info("Connexion SSE terminée pour un émetteur.");
            this.emitters.remove(emitter);
        });

        emitter.onTimeout(() -> {
            LOGGER.info("Timeout SSE expiré pour un émetteur.");
            emitter.complete();
            this.emitters.remove(emitter);
        });

        emitter.onError((ex) -> {
            LOGGER.error("Erreur détectée sur un émetteur SSE.", ex);
            emitter.completeWithError(ex);
            this.emitters.remove(emitter);
        });

        // Envoi d'un événement d'initialisation pour confirmer la connexion
        try {
            emitter.send(SseEmitter.event()
                    .name("init")
                    .data("Connexion établie avec le service de facturation en temps réel"));
        } catch (IOException e) {
            LOGGER.error("Impossible d'envoyer l'événement d'initialisation SSE.", e);
            emitter.completeWithError(e);
            this.emitters.remove(emitter);
        }

        return emitter;
    }

    /**
     * Notifie tous les clients abonnés d'un changement d'état d'une facture.
     */
    public void notifyInvoiceChange(InvoiceDto invoice) {
        if (emitters.isEmpty()) {
            return;
        }

        LOGGER.info("Propagation du changement de statut de la facture en temps réel. Ref={}, Statut={}, Nombre d'abonnés={}",
                invoice.getReference(), invoice.getStatus(), emitters.size());

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("invoice-update")
                        .data(invoice));
            } catch (IOException | IllegalStateException e) {
                LOGGER.warn("Émetteur SSE inactif ou déconnecté. Suppression de la liste.");
                deadEmitters.add(emitter);
            }
        }

        if (!deadEmitters.isEmpty()) {
            emitters.removeAll(deadEmitters);
        }
    }
}
