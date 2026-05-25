package ma.atos.billing.invoice.billing_invoice.mappers;


import ma.atos.billing.invoice.billing_invoice.dtos.InvoiceDto;
import ma.atos.billing.invoice.billing_invoice.entities.Creancier;
import ma.atos.billing.invoice.billing_invoice.entities.Customer;
import ma.atos.billing.invoice.billing_invoice.entities.Invoice;
import ma.atos.billing.invoice.billing_invoice.entities.PointDeVente;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public InvoiceDto toDto(Invoice entity) {
        if (entity == null) {
            return null;
        }

        Long customerId = entity.getCustomer() != null ? entity.getCustomer().getId() : null;
        Long creancierId = entity.getCreancier() != null ? entity.getCreancier().getId() : null;
        Long pointDeVenteId = entity.getPointDeVente() != null ? entity.getPointDeVente().getId() : null;

        return new InvoiceDto(
                entity.getId(),
                entity.getReference(),
                entity.getDateInvoice(),
                entity.getDateDue(),
                entity.getMontantHt(),
                entity.getMontantTva(),
                entity.getMontantTtc(),
                entity.getStatus(),
                entity.getModeReglement(),
                entity.getDescription(),
                customerId,
                creancierId,
                pointDeVenteId,
                entity.getCreatedDate(),
                entity.getUpdatedDate()
        );
    }

    /**
     * Create a new invoice entity. Caller provides resolved relation entities.
     */
    public Invoice toEntity(InvoiceDto dto, Customer customer, Creancier creancier, PointDeVente pointDeVente) {
        if (dto == null) {
            return null;
        }

        Invoice entity = new Invoice();
        entity.setId(dto.getId());
        updateEntity(entity, dto, customer, creancier, pointDeVente);
        entity.setCreatedDate(dto.getCreatedDate());
        entity.setUpdatedDate(dto.getUpdatedDate());
        return entity;
    }

    public void updateEntity(Invoice entity, InvoiceDto dto, Customer customer, Creancier creancier, PointDeVente pointDeVente) {
        if (entity == null || dto == null) {
            return;
        }

        entity.setReference(dto.getReference());
        entity.setDateInvoice(dto.getDateInvoice());
        entity.setDateDue(dto.getDateDue());
        entity.setMontantHt(dto.getMontantHt());
        entity.setMontantTva(dto.getMontantTva());
        entity.setMontantTtc(dto.getMontantTtc());
        entity.setStatus(dto.getStatus());
        entity.setModeReglement(dto.getModeReglement());
        entity.setDescription(dto.getDescription());

        entity.setCustomer(customer);
        entity.setCreancier(creancier);
        entity.setPointDeVente(pointDeVente);
    }
}
