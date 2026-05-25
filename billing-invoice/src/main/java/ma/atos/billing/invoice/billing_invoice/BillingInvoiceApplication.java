package ma.atos.billing.invoice.billing_invoice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BillingInvoiceApplication {

	public static void main(String[] args) {
		// Point d'entree de l'application Spring Boot.
		// Cette instruction demarre le serveur embarque et charge les beans Spring.
		SpringApplication.run(BillingInvoiceApplication.class, args);
	}

}
