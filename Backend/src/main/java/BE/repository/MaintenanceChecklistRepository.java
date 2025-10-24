package BE.repository;

import BE.entity.MaintenanceChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceChecklistRepository extends JpaRepository<MaintenanceChecklist, Long> {

    List<MaintenanceChecklist> findByMaintenance_MaintenanceID(Long maintenanceId);

    Optional<MaintenanceChecklist> findByMaintenance_MaintenanceIDAndCheckList_CheckListId(Long maintenanceId, Long checkListId);
}