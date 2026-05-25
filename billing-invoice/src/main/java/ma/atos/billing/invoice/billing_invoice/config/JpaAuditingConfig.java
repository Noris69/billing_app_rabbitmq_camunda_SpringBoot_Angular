package ma.atos.billing.invoice.billing_invoice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
// Active le remplissage automatique des champs @CreatedDate et @LastModifiedDate.
@EnableJpaAuditing
public class JpaAuditingConfig {
}
