package ma.atos.billing.invoice.billing_invoice.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.atos.billing.invoice.billing_invoice.enums.ModeReglement;
import ma.atos.billing.invoice.billing_invoice.enums.StatusInvoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {

    private Long id;

    @NotBlank
    private String reference;

    private LocalDate dateInvoice;

    private LocalDate dateDue;

    @PositiveOrZero
    private BigDecimal montantHt;

    @PositiveOrZero
    private BigDecimal montantTva;

    @NotNull
    @Positive
    private BigDecimal montantTtc;

    private StatusInvoice status;

    @NotNull
    private ModeReglement modeReglement;

    private String description;

    // Relations -> IDs (évite les boucles JSON et dépendances JPA)
    @NotNull
    private Long customerId;

    @NotNull
    private Long creancierId;

    @NotNull
    private Long pointDeVenteId;

    private Date createdDate;

    private Date updatedDate;
}

