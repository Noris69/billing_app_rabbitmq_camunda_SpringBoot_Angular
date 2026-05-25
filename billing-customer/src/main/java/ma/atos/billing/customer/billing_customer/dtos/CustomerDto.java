package ma.atos.billing.customer.billing_customer.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {

    private Long id;
    private String nom;
    private String prenom;
    private String adresse;
    private String paymentType;
    private Date createdDate;
    private Date updatedDate;
}
