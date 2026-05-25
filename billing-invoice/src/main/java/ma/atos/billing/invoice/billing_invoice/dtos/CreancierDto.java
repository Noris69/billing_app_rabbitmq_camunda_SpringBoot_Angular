package ma.atos.billing.invoice.billing_invoice.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.atos.billing.invoice.billing_invoice.enums.TypeCreancier;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreancierDto {

    private Long id;

    @NotBlank
    private String nom;

    @NotNull
    private TypeCreancier typeCreancier;

    private String ice;

    private String rc;

    private String rib;

    private String banque;

    @Email
    private String email;

    private String telephone;

    private String adresse;

    private Date createdDate;

    private Date updatedDate;
}
