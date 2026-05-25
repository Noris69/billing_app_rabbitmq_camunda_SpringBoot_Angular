package ma.atos.billing.invoice.billing_invoice.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {


    private Long id;
    @NotBlank
    private String nom;

    @NotBlank
    private String prenom;

    @NotBlank
    private String cin;

    @Email
    private String email;

    private String telephone;

    private String adresse;

    private String ville;

    private Date createdDate;

    private Date updatedDate;
}
