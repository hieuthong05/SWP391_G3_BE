package BE.repository;

import BE.entity.MaintenanceComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceComponentRepository extends JpaRepository<MaintenanceComponent, Long> {

    // Tìm tất cả MaintenanceComponent theo maintenanceID
    List<MaintenanceComponent> findByMaintenance_MaintenanceID(Long maintenanceId);

    // Tìm một MaintenanceComponent cụ thể trong một Maintenance theo ComponentID
    Optional<MaintenanceComponent> findByMaintenance_MaintenanceIDAndComponent_ComponentID(Long maintenanceId, Long componentId);
}