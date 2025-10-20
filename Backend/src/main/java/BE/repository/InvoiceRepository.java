package BE.repository;

import BE.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByMaintenance_MaintenanceID(Long maintenanceId);
    boolean existsByMaintenance_MaintenanceID(Long maintenanceId);
}