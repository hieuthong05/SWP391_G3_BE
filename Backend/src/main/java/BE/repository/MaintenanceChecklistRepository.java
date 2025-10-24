package BE.repository;

import BE.entity.MaintenanceChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaintenanceChecklistRepository extends JpaRepository<MaintenanceChecklist, Long> {

    @Query("SELECT mc FROM MaintenanceChecklist mc JOIN FETCH mc.checkList WHERE mc.maintenance.maintenanceID = :maintenanceId")
    List<MaintenanceChecklist> findByMaintenance_MaintenanceIDWithChecklist(@Param("maintenanceId") Long maintenanceId);

    List<MaintenanceChecklist> findByMaintenance_MaintenanceID(Long maintenanceId);

    Optional<MaintenanceChecklist> findByMaintenance_MaintenanceIDAndCheckList_CheckListId(Long maintenanceId, Long checkListId);
}