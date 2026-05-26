package ma.atos.billing.invoice.billing_invoice.services;

import ma.atos.billing.invoice.billing_invoice.dtos.InvoiceReportDto;
import ma.atos.billing.invoice.billing_invoice.entities.Invoice;
import ma.atos.billing.invoice.billing_invoice.repository.InvoiceRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class InvoiceReportService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceReportService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public byte[] exportInvoiceReceiptPdf(Long invoiceId) throws JRException {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Facture introuvable pour l'id : " + invoiceId));

        InvoiceReportDto reportDto = mapToReportDto(invoice);

        InputStream reportStream = getClass().getResourceAsStream("/reports/invoice-receipt.jrxml");
        if (reportStream == null) {
            throw new IllegalStateException("Template Jasper introuvable : /reports/invoice-receipt.jrxml");
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
        
        // On enveloppe le DTO dans une collection à élément unique
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(reportDto));

        Map<String, Object> parameters = new HashMap<>();

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    private InvoiceReportDto mapToReportDto(Invoice entity) {
        InvoiceReportDto dto = new InvoiceReportDto();
        dto.setId(entity.getId());
        dto.setReference(entity.getReference());
        dto.setDateInvoice(entity.getDateInvoice() != null ? entity.getDateInvoice().toString() : "-");
        dto.setDateDue(entity.getDateDue() != null ? entity.getDateDue().toString() : "-");
        dto.setMontantHt(entity.getMontantHt());
        dto.setMontantTva(entity.getMontantTva());
        dto.setMontantTtc(entity.getMontantTtc());
        dto.setStatus(entity.getStatus() != null ? entity.getStatus().name() : "-");
        dto.setModeReglement(entity.getModeReglement() != null ? entity.getModeReglement().name() : "Non spécifié");
        dto.setDescription(entity.getDescription() != null ? entity.getDescription() : "");

        if (entity.getCustomer() != null) {
            dto.setCustomerName(entity.getCustomer().getNom() + " " + entity.getCustomer().getPrenom());
            dto.setCustomerAdresse(entity.getCustomer().getAdresse());
        } else {
            dto.setCustomerName("Client Inconnu");
            dto.setCustomerAdresse("-");
        }

        if (entity.getCreancier() != null) {
            dto.setCreancierNom(entity.getCreancier().getNom());
            dto.setCreancierIce(entity.getCreancier().getIce());
            dto.setCreancierRc(entity.getCreancier().getRc());
            dto.setCreancierRib(entity.getCreancier().getRib());
            dto.setCreancierBanque(entity.getCreancier().getBanque());
        } else {
            dto.setCreancierNom("Créancier Inconnu");
        }

        if (entity.getPointDeVente() != null) {
            dto.setPointDeVenteNom(entity.getPointDeVente().getNom());
        } else {
            dto.setPointDeVenteNom("Système Central / IHM");
        }

        return dto;
    }
}
