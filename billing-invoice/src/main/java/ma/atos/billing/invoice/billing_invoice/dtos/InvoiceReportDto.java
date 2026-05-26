package ma.atos.billing.invoice.billing_invoice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceReportDto {
    private Long id;
    private String reference;
    private String dateInvoice;
    private String dateDue;
    private Double montantHt;
    private Double montantTva;
    private Double montantTtc;
    private String status;
    private String modeReglement;
    private String description;
    
    private String customerName;
    private String customerAdresse;
    
    private String creancierNom;
    private String creancierIce;
    private String creancierRc;
    private String creancierRib;
    private String creancierBanque;
    
    private String pointDeVenteNom;
}
