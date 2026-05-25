package ma.atos.billing.invoice.billing_invoice;

import org.springframework.boot.SpringApplication;

public class TestBillingInvoiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(BillingInvoiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
