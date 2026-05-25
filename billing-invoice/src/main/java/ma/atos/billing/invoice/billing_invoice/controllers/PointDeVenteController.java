package ma.atos.billing.invoice.billing_invoice.controllers;

import jakarta.validation.Valid;
import ma.atos.billing.invoice.billing_invoice.dtos.PointDeVenteDto;
import ma.atos.billing.invoice.billing_invoice.dtos.PointDeVenteSearchCriteria;
import ma.atos.billing.invoice.billing_invoice.services.PointDeVenteReportService;
import ma.atos.billing.invoice.billing_invoice.services.PointDeventeService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/points-de-vente", "/api/point-de-vente"})
public class PointDeVenteController {

    private final PointDeventeService service;
    private final PointDeVenteReportService reportService;

    public PointDeVenteController(PointDeventeService service, PointDeVenteReportService reportService) {
        this.service = service;
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<PointDeVenteDto> create(@Valid @RequestBody PointDeVenteDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PointDeVenteDto> update(@PathVariable("id") Long id, @Valid @RequestBody PointDeVenteDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<PointDeVenteDto> getById(@PathVariable("id") long id) {
        return ResponseEntity.ok(service.getPointDeVenteById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PointDeVenteDto>> searchPointDeVente(
            PointDeVenteSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(service.searchPointDeVente(criteria, page, 10));
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(PointDeVenteSearchCriteria criteria) throws JRException {
        byte[] pdf = reportService.exportPdf(criteria);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=points-de-vente.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}
