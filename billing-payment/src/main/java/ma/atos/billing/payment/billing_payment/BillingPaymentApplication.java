package ma.atos.billing.payment.billing_payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BillingPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingPaymentApplication.class, args);
    }
}
