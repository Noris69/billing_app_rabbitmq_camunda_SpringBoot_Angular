package ma.atos.billing.payment.billing_payment;

import com.fasterxml.jackson.databind.JsonNode;
import ma.atos.billing.payment.billing_payment.dtos.PaymentRequestDto;
import ma.atos.billing.payment.billing_payment.enums.ModeReglement;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.cloud.vault.enabled=false",
        "spring.cloud.vault.kv.enabled=false",
        "spring.flyway.create-schemas=true",
        "spring.flyway.schemas=payment",
        "spring.flyway.default-schema=payment",
        "spring.jpa.hibernate.ddl-auto=validate",
        "management.tracing.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration,org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration"
})
class PaymentControllerIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("payment_it")
            .withUsername("test")
            .withPassword("test");

    @Container
    static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:3-management");

    @LocalServerPort
    int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.rabbitmq.host", RABBIT::getHost);
        registry.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBIT::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBIT::getAdminPassword);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "http://localhost/realms/test");
    }

    @Test
    void createGetAndMarkPaymentSuccess() {
        PaymentRequestDto request = new PaymentRequestDto(
                99001L,
                "INV-IT-99001",
                9001L,
                9101L,
                9201L,
                new BigDecimal("120.50"),
                "MAD",
                ModeReglement.CARTE,
                "Paiement integration test",
                null,
                null
        );

        ResponseEntity<JsonNode> created = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/payments",
                request,
                JsonNode.class
        );

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode body = created.getBody();
        assertThat(body.path("status").asText()).isEqualTo("PENDING");
        assertThat(body.path("modeReglement").asText()).isEqualTo("CARTE");
        assertThat(body.path("amount").decimalValue()).isEqualByComparingTo("120.50");

        long id = body.path("id").asLong();
        ResponseEntity<JsonNode> fetched = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/payments/" + id,
                JsonNode.class
        );

        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody().path("invoiceReference").asText()).isEqualTo("INV-IT-99001");

        ResponseEntity<JsonNode> markedSuccess = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/payments/" + id + "/mark-success",
                null,
                JsonNode.class
        );

        assertThat(markedSuccess.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(markedSuccess.getBody().path("status").asText()).isEqualTo("SUCCESS");
    }
}
