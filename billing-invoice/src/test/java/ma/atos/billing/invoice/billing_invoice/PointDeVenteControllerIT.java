package ma.atos.billing.invoice.billing_invoice;

import ma.atos.billing.invoice.billing_invoice.dtos.PointDeVenteDto;
import ma.atos.billing.invoice.billing_invoice.dtos.PointDeVenteType;
import ma.atos.billing.invoice.billing_invoice.repository.PointDeVenteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.flyway.create-schemas=true",
        "spring.flyway.baseline-on-migrate=false",
        "spring.flyway.init-sqls=CREATE SEQUENCE IF NOT EXISTS global_sequence START WITH 1 INCREMENT BY 1",
        "spring.flyway.schemas=invoice",
        "spring.flyway.default-schema=invoice",
        "spring.jpa.hibernate.ddl-auto=validate"
})
class PointDeVenteControllerIT {

    private static final String BASE_URL = "/api/points-de-vente";

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PointDeVenteRepository repository;

    @AfterEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void createShouldPersistAgence() {
        PointDeVenteDto request = agenceRequest();

        ResponseEntity<PointDeVenteDto> response = restTemplate.postForEntity(BASE_URL, request, PointDeVenteDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getType()).isEqualTo(PointDeVenteType.AGENCE);
        assertThat(response.getBody().getNom()).isEqualTo("Agence Centre");
        assertThat(response.getBody().getCodeAgence()).isEqualTo("AG001");
        assertThat(repository.existsById(response.getBody().getId())).isTrue();
    }

    @Test
    void updateShouldModifyExistingDistributeur() {
        PointDeVenteDto created = createDistributeur();
        PointDeVenteDto updateRequest = distributeurRequest();
        updateRequest.setNom("Distributeur Nord Modifie");
        updateRequest.setZoneDistribution("Nord-Ouest");
        updateRequest.setCommission(15.0);

        ResponseEntity<PointDeVenteDto> response = restTemplate.exchange(
                BASE_URL + "/" + created.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                PointDeVenteDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(created.getId());
        assertThat(response.getBody().getType()).isEqualTo(PointDeVenteType.DISTRIBUTEUR);
        assertThat(response.getBody().getNom()).isEqualTo("Distributeur Nord Modifie");
        assertThat(response.getBody().getZoneDistribution()).isEqualTo("Nord-Ouest");
        assertThat(response.getBody().getCommission()).isEqualTo(15.0);
    }

    @Test
    void deleteShouldRemoveExistingPointDeVente() {
        PointDeVenteDto created = createAgence();

        ResponseEntity<Void> response = restTemplate.exchange(
                BASE_URL + "/" + created.getId(),
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(repository.existsById(created.getId())).isFalse();
    }

    private PointDeVenteDto createAgence() {
        return restTemplate.postForEntity(BASE_URL, agenceRequest(), PointDeVenteDto.class).getBody();
    }

    private PointDeVenteDto createDistributeur() {
        return restTemplate.postForEntity(BASE_URL, distributeurRequest(), PointDeVenteDto.class).getBody();
    }

    private PointDeVenteDto agenceRequest() {
        PointDeVenteDto dto = new PointDeVenteDto();
        dto.setType(PointDeVenteType.AGENCE);
        dto.setNom("Agence Centre");
        dto.setAdresse("Casablanca Maarif");
        dto.setTelephone("0522000000");
        dto.setCodeAgence("AG001");
        dto.setResponsable("Ahmed Alami");
        dto.setRegion("Casablanca-Settat");
        dto.setTypeAgence("PRINCIPALE");
        return dto;
    }

    private PointDeVenteDto distributeurRequest() {
        PointDeVenteDto dto = new PointDeVenteDto();
        dto.setType(PointDeVenteType.DISTRIBUTEUR);
        dto.setNom("Distributeur Nord");
        dto.setAdresse("Tanger Centre");
        dto.setTelephone("0539000000");
        dto.setCodeDistributeur("DIST001");
        dto.setZoneDistribution("Nord");
        dto.setNomCommercial("Nord Distribution");
        dto.setCommission(12.5);
        return dto;
    }
}
