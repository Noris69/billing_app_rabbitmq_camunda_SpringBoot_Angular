package ma.atos.billing.invoice.billing_invoice.repository;


import ma.atos.billing.invoice.billing_invoice.entities.Invoice;
import ma.atos.billing.invoice.billing_invoice.enums.StatusInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {

    Optional<Invoice> findByReference(String reference);

    List<Invoice> findByStatus(StatusInvoice status);

    Page<Invoice> findByStatus(StatusInvoice status, Pageable pageable);

    Page<Invoice> findByReferenceContainingIgnoreCase(String reference, Pageable pageable);

    List<Invoice> findByCustomer_Id(Long customerId);

    Page<Invoice> findByCustomer_Id(Long customerId, Pageable pageable);

    List<Invoice> findByCreancier_Id(Long creancierId);

    Page<Invoice> findByCreancier_Id(Long creancierId, Pageable pageable);

    List<Invoice> findByPointDeVente_Id(Long pointDeVenteId);

    Page<Invoice> findByPointDeVente_Id(Long pointDeVenteId, Pageable pageable);
}
