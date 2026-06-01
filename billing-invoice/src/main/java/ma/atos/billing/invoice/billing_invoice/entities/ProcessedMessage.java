package ma.atos.billing.invoice.billing_invoice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "processed_message", schema = "invoice")
public class ProcessedMessage {

    @Id
    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "event_type", length = 100)
    private String eventType;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    public ProcessedMessage(String eventId, String eventType) {
        this.eventId = eventId;
        this.eventType = eventType;
    }

    @PrePersist
    void prePersist() {
        if (processedAt == null) {
            processedAt = LocalDateTime.now();
        }
    }
}
