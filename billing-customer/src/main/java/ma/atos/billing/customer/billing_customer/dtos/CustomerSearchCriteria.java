package ma.atos.billing.customer.billing_customer.dtos;

import lombok.Data;

@Data
public class CustomerSearchCriteria {

    private String nom;
    private String prenom;
    private String adresse;
    private String paymentType;
    private String query;
}
