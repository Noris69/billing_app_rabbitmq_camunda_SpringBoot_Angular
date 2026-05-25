package ma.atos.billing.invoice.billing_invoice.services;

import ma.atos.billing.invoice.billing_invoice.dtos.PointDeVenteDto;
import ma.atos.billing.invoice.billing_invoice.dtos.PointDeVenteSearchCriteria;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PointDeVenteReportService {

    private static final int EXPORT_MAX_SIZE = 10_000;

    private final PointDeventeService pointDeventeService;

    public PointDeVenteReportService(PointDeventeService pointDeventeService) {
        this.pointDeventeService = pointDeventeService;
    }

    public byte[] exportPdf(PointDeVenteSearchCriteria criteria) throws JRException {
        List<PointDeVenteDto> pointsDeVente = pointDeventeService
                .searchPointDeVente(criteria, 0, EXPORT_MAX_SIZE)
                .getContent();

        InputStream reportStream = getClass().getResourceAsStream("/reports/points-de-vente.jrxml");
        if (reportStream == null) {
            throw new IllegalStateException("Template Jasper introuvable : /reports/points-de-vente.jrxml");
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(pointsDeVente);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("REPORT_TITLE", "Liste des points de vente");

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}
