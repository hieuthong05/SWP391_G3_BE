package BE.repository;

import BE.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByMaintenance_MaintenanceID(Long maintenanceId);
    boolean existsByMaintenance_MaintenanceID(Long maintenanceId);

    @Query(value = """
    SELECT 
        EXTRACT(MONTH FROM i.issued_date) AS month,
        EXTRACT(YEAR FROM i.issued_date) AS year,
        SUM(i.total_amount) AS totalRevenue
    FROM invoice i
    WHERE i.status = 'PAID'
    GROUP BY EXTRACT(YEAR FROM i.issued_date), EXTRACT(MONTH FROM i.issued_date)
    ORDER BY EXTRACT(YEAR FROM i.issued_date), EXTRACT(MONTH FROM i.issued_date)
    """, nativeQuery = true)
    List<Map<String, Object>> getMonthlyRevenue();
}